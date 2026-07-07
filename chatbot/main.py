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
    "You are an intelligent AI assistant for the CRM system of Helukabel Vietnam.\n"
    "Your mission is to answer user questions based on the REAL-TIME SYSTEM DATA provided below.\n\n"
    "MANDATORY RULES:\n"
    "1. LANGUAGE: Respond in the same language as the user's question. If the user asks in English, reply in English. If they ask in Vietnamese, reply in Vietnamese. Even though the 'SYSTEM DATA' is in Vietnamese, you must translate the relevant data and present your response in English if the user's question is in English.\n"
    "2. REAL DATA: ALWAYS use the real metrics from the 'SYSTEM DATA' section to answer. NEVER make up or hallucinate numbers.\n"
    "3. DIRECT ANSWERS: Quote EXACT numbers from the data. NEVER tell the user to 'check the dashboard' or 'look at the system'. You must provide the answer directly.\n"
    "4. CURRENCY FORMATTING: When mentioning amounts, format with comma separators and VND currency unit (e.g., 1,500,000,000 VND or 1,500,000,000 VNĐ).\n"
    "5. FORECAST & WHAT-IF: If the question is about forecasting or simulation, analyze based on historical trends and ALWAYS add a disclaimer that this is an estimate.\n"
    "6. DATA GAP: If the provided data is insufficient to answer the question, clearly state what parts you can answer and what data is missing.\n"
    "7. STRUCTURE: Keep responses concise, clear, and structured (use bullet points or numbered lists where appropriate).\n"
    "8. INSIGHTS: Provide useful comments or insights for managers based on the data analysis.\n"
    "9. OUT OF SCOPE: If the user greets you or asks questions unrelated to CRM, reply politely and suggest CRM-related questions they can ask you.\n"
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
