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
from chatbot.intent_detector import detect_intent
from chatbot.analyzers import (
    get_dashboard_context,
    make_ai_summary, format_status_table, answer_status_count,
    analyze_lead_source, analyze_sales_owner,
    analyze_pipeline, analyze_lost,
    analyze_revenue, analyze_forecast
)

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
    version="2.0.0",
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
# MAIN CHATBOT LOGIC
# ==================
async def ask_dashboard(question: str) -> str:
    """Process a question and return the chatbot response."""
    intent = detect_intent(question)
    logger.info(f"Question: {question[:100]}... | Intent: {intent}")

    # --- SALES OWNER ---
    if intent in [
        "best_seller_revenue", "top_5_seller_revenue", "revenue_seller_bottom5",
        "best_seller_win_rate", "seller_most_open_leads", "seller_fastest"
    ]:
        return await analyze_sales_owner(client, intent)

    # --- LEAD SOURCE ---
    if intent in [
        "best_source_leads", "best_source_conversion",
        "best_source_revenue", "best_source_roi",
        "source_cpl", "source_cpw"
    ]:
        return await analyze_lead_source(client, intent)

    # --- PIPELINE COVERAGE ---
    if intent in ["pipeline_current", "pipeline_best_seller", "pipeline_enough_target"]:
        return await analyze_pipeline(client, intent)

    # --- LOST ANALYSIS ---
    if intent in [
        "lost_reason_most_common", "lost_reason_price", "lost_rate_current",
        "total_lost", "lost_reason_list", "lost_reason_highest_rate",
        "lost_seller_highest_rate", "lost_seller_most_count",
        "lost_source_most", "lost_region_most", "lost_industry_most",
        "top_5_lost_reasons"
    ]:
        return await analyze_lost(client, intent, question)

    # --- REVENUE ---
    if intent.startswith("revenue_"):
        return await analyze_revenue(client, intent, question)

    # --- FORECAST & WHAT-IF ---
    if intent.startswith("forecast_") or intent.startswith("what_if_"):
        return await analyze_forecast(client, intent, question)

    # --- LEAD OVERVIEW ---
    ctx = await get_dashboard_context(client)

    if ctx["total"] == 0 and not ctx["status_data"]:
        return "Tôi chưa lấy được dữ liệu từ API /lead-status. Vui lòng kiểm tra Spring Boot backend."

    if intent == "lead_summary":
        return make_ai_summary(ctx)
    if intent == "lead_status":
        return format_status_table(ctx)
    if intent == "lead_new":
        return answer_status_count(ctx, "new", "New")
    if intent == "lead_connected":
        return answer_status_count(ctx, "connected", "Connected")
    if intent == "lead_qualified":
        return answer_status_count(ctx, "qualified", "Qualified")
    if intent == "lead_won":
        return answer_status_count(ctx, "won", "Won")
    if intent == "lead_lost":
        return answer_status_count(ctx, "lost", "Lost")
    if intent == "won_rate":
        return f"Tỉ lệ Won hiện tại là {ctx['won_rate']}%."
    if intent == "lost_rate":
        return f"Tỉ lệ Lost hiện tại là {ctx['lost_rate']}%."

    # --- UNKNOWN ---
    return (
        "Tôi chưa hiểu rõ câu hỏi.\n"
        "Bạn có thể hỏi về:\n"
        "• Tổng quan lead (VD: 'Tình hình lead hiện tại?')\n"
        "• Doanh thu (VD: 'Doanh thu theo seller như thế nào?')\n"
        "• Seller (VD: 'Seller nào doanh thu cao nhất?')\n"
        "• Lead source (VD: 'Nguồn nào mang lại nhiều lead?')\n"
        "• Lost analysis (VD: 'Lý do Lost phổ biến nhất?')\n"
        "• Pipeline (VD: 'Pipeline coverage hiện tại?')\n"
        "• Dự báo (VD: 'Dự đoán doanh thu tháng sau?')"
    )

# ==================
# API ROUTES
# ==================
@app.post("/ask")
async def ask(request: QuestionRequest):
    """Ask the chatbot a question about CRM dashboard data."""
    answer = await ask_dashboard(request.question)
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
        "version": "2.0.0",
        "backend_url": BASE_URL
    }
