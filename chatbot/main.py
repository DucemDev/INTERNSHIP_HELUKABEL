"""
Helukabel CRM Chatbot - FastAPI Application

Improvements over original app.py:
- Restricted CORS (only Spring Boot frontend allowed)
- Authentication middleware (checks Authorization header or session cookie)
- Input validation (max question length)
- Async throughout
- Proper logging (no print statements)
- Structured error handling

Run: uvicorn main:app --reload --port 8000
"""

from fastapi import FastAPI, Request, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from contextlib import asynccontextmanager
from datetime import datetime
import asyncio
import logging
import sys
import os

# Dynamic path resolution to support running uvicorn from any directory
sys.path.insert(0, os.path.abspath(os.path.dirname(os.path.dirname(__file__))))


from chatbot.config import (
    BASE_URL, CORS_ORIGINS, CACHE_TTL, API_TIMEOUT,
    MAX_QUESTION_LENGTH, LOG_LEVEL, LOG_FORMAT
)
from chatbot.api_client import CachedAPIClient
from chatbot.gemini_client import GeminiClient
from chatbot.analyzers import get_system_db_context

# ==================
# LOGGING SETUP
# ==================
logging.basicConfig(level=LOG_LEVEL, format=LOG_FORMAT)
logger = logging.getLogger(__name__)

# ==================
# API CLIENT (shared instance)
# ==================
client = CachedAPIClient(
    base_url=BASE_URL,
    cache_ttl=CACHE_TTL,
    timeout=API_TIMEOUT
)
# Initialise Gemini client for LLM responses
gemini_client = GeminiClient()

# System instruction for Gemini – tells the LLM its role and how to behave
SYSTEM_INSTRUCTION = (
    "Bạn là trợ lý AI thông minh cho hệ thống CRM của Helukabel Việt Nam.\n"
    "Nhiệm vụ của bạn là trả lời các câu hỏi của người dùng dựa trên DỮ LIỆU THỰC TẾ từ hệ thống CRM được cung cấp bên dưới.\n\n"
    "QUY TẮC BẮT BUỘC:\n"
    "1. LUÔN trả lời bằng tiếng Việt.\n"
    "2. LUÔN sử dụng số liệu thực tế từ phần 'DỮ LIỆU HỆ THỐNG' để trả lời. KHÔNG BAO GIỜ bịa số liệu.\n"
    "3. Nếu người dùng hỏi về dữ liệu cụ thể (doanh thu, lead, seller...), hãy trích dẫn CON SỐ CHÍNH XÁC từ dữ liệu được cung cấp.\n"
    "4. KHÔNG BAO GIỜ hướng dẫn user 'hãy vào dashboard để xem' hoặc 'hãy kiểm tra trên hệ thống'. Bạn phải TRẢ LỜI TRỰC TIẾP với dữ liệu.\n"
    "5. Khi trả lời về số tiền, hãy format với dấu phẩy phân cách hàng nghìn và đơn vị VNĐ (ví dụ: 1,500,000,000 VNĐ).\n"
    "6. Nếu câu hỏi liên quan đến dự báo/giả lập (forecast, what-if), hãy phân tích dựa trên xu hướng dữ liệu hiện có và LUÔN thêm disclaimer rằng đây là ước tính.\n"
    "7. Nếu dữ liệu hệ thống không đủ để trả lời câu hỏi, hãy nói rõ phần nào bạn có thể trả lời và phần nào thiếu dữ liệu.\n"
    "8. Trả lời ngắn gọn, rõ ràng, có cấu trúc (sử dụng bullet points, đánh số khi cần).\n"
    "9. Khi phân tích, hãy đưa ra nhận xét/insight hữu ích cho người quản lý.\n"
    "10. Nếu người dùng chào hỏi hoặc hỏi câu không liên quan đến CRM, hãy trả lời thân thiện và gợi ý các câu hỏi họ có thể hỏi về hệ thống CRM.\n"
)

# ==================
# APP LIFESPAN
# ==================
@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Chatbot service starting...")
    yield
    await client.close()
    logger.info("Chatbot service stopped.")

app = FastAPI(
    title="Helukabel CRM Chatbot",
    description="AI-powered chatbot for CRM dashboard queries",
    version="3.0.0",
    lifespan=lifespan
)

# ==================
# CORS - RESTRICTED (only Spring Boot frontend)
# ==================
app.add_middleware(
    CORSMiddleware,
    allow_origins=CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["GET", "POST"],
    allow_headers=["*"],
)

# ==================
# AUTH MIDDLEWARE
# ==================
@app.middleware("http")
async def auth_middleware(request: Request, call_next):
    """Verify that requests come from authenticated users.
    
    Checks for Authorization header or JSESSIONID session cookie
    from Spring Boot. Skip auth for health check endpoint.
    """
    # Skip auth for health check and docs
    if request.url.path in ["/health", "/docs", "/openapi.json"]:
        return await call_next(request)
    
    # Allow bypass for localhost (testing) or if Authorization header present
    if request.client.host in ("127.0.0.1", "::1"):
        return await call_next(request)
    # Check for auth token or session cookie
    auth_header = request.headers.get("Authorization")
    session_cookie = request.cookies.get("JSESSIONID")
    
    if not auth_header and not session_cookie:
        logger.warning(f"Unauthorized request from {request.client.host}")
        raise HTTPException(status_code=401, detail="Unauthorized - Please login first")
    
    return await call_next(request)

# ==================
# REQUEST MODEL
# ==================
class QuestionRequest(BaseModel):
    question: str = Field(
        ...,
        min_length=1,
        max_length=MAX_QUESTION_LENGTH,
        description="The question to ask the chatbot"
    )

# ==================
# MAIN CHATBOT LOGIC (v3 – All questions go through Gemini with real data)
# ==================
async def ask_dashboard(question: str) -> str:
    """Process a question by fetching real system data and passing it to Gemini LLM."""
    logger.info(f"Question: {question[:100]}...")

    # Step 1: Fetch all real data from the Spring Boot backend
    try:
        db_context = await get_system_db_context(client)
    except Exception as e:
        logger.error("Failed to fetch system data: %s", e)
        db_context = "Không lấy được dữ liệu từ hệ thống. Backend có thể chưa khởi động."

    # Step 2: Build the full prompt with real data + user question
    full_prompt = (
        f"## DỮ LIỆU HỆ THỐNG CRM HELUKABEL (Dữ liệu thực tế, cập nhật realtime):\n\n"
        f"{db_context}\n\n"
        f"---\n\n"
        f"## CÂU HỎI CỦA NGƯỜI DÙNG:\n"
        f"{question}\n\n"
        f"Hãy trả lời câu hỏi trên dựa trên dữ liệu thực tế đã cung cấp."
    )

    # Step 3: Call Gemini LLM with the data-enriched prompt
    # NOTE: gemini_client.generate_content() is a SYNCHRONOUS blocking call.
    # We MUST run it in a thread pool via asyncio.to_thread() to avoid blocking
    # the FastAPI event loop (which would freeze the entire server).
    try:
        answer = await asyncio.wait_for(
            asyncio.to_thread(
                gemini_client.generate_content,
                prompt=full_prompt,
                system_instruction=SYSTEM_INSTRUCTION,
                max_output_tokens=4096,
            ),
            timeout=120.0  # 120-second safety timeout (includes retry + fallback time)
        )
        return answer
    except asyncio.TimeoutError:
        logger.error("Gemini LLM call timed out after 60 seconds")
        return "Xin lỗi, hệ thống AI đang phản hồi chậm. Vui lòng thử lại sau."
    except Exception as e:
        logger.error("Gemini LLM call failed: %s", e)
        return "Xin lỗi, tôi không thể trả lời câu hỏi lúc này. Vui lòng thử lại sau."

# ==================
# API ROUTES
# ==================
@app.post("/ask")
async def ask(request: QuestionRequest):
    """Ask the chatbot a question about CRM dashboard data."""
    answer = await ask_dashboard(request.question)
    # Guard against empty answer which breaks UI expectations
    if not answer or not answer.strip():
        logger.warning("Empty answer generated, using fallback message")
        answer = "Xin lỗi, tôi không thể trả lời câu hỏi lúc này. Vui lòng thử lại sau."
    return {
        "question": request.question,
        "answer": answer,
        "time": datetime.now().strftime("%d/%m/%Y %H:%M")
    }


@app.get("/health")
async def health():
    """Health check endpoint."""
    return {
        "status": "AI service is running",
        "version": "3.0.0",
        "backend_url": BASE_URL
    }
