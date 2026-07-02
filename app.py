from fastapi import FastAPI
from pydantic import BaseModel
import requests
from datetime import datetime
import unicodedata
import contextvars
import re

app = FastAPI()

# Context variables for year/quarter filters (thread/async-safe)
current_year = contextvars.ContextVar('current_year', default=None)
current_quarter = contextvars.ContextVar('current_quarter', default=None)
from fastapi.middleware.cors import CORSMiddleware

# Thêm cấu hình này để Frontend gọi được API của bạn
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # Cho phép tất cả các nguồn (website/domain) kết nối
    allow_credentials=True,
    allow_methods=["*"], # Cho phép mọi phương thức (GET, POST,...)
    allow_headers=["*"], # Cho phép mọi loại header
)
BASE_URL = "http://localhost:8080/api/dashboard"
DEBUG = False


class QuestionRequest(BaseModel):
    question: str

def remove_accents(text):
    text = unicodedata.normalize("NFD", text)
    text = text.encode("ascii", "ignore").decode("utf-8")
    return text


def normalize_question(question):
    q = question.lower().strip()
    q_no_accent = remove_accents(q)

    return q, q_no_accent


def has_any(text, keywords):
    return any(keyword in text for keyword in keywords)

def call_api(endpoint):
    url = BASE_URL + endpoint
    
    # Retrieve active year/quarter filters from context
    params = {}
    y = current_year.get()
    q = current_quarter.get()
    if y:
        params['year'] = y
    if q:
        params['quarter'] = q

    try:
        response = requests.get(url, params=params)

        if DEBUG:
            print("URL:", url)
            print("Status:", response.status_code)
            print("Content-Type:", response.headers.get("Content-Type"))
            print("Response text:", response.text[:500])

        if response.status_code != 200:
            return None

        if "application/json" not in response.headers.get("Content-Type", ""):
            return None

        return response.json()

    except Exception:
        return None


# =========================
# LEAD OVERVIEW
# =========================

def normalize_status_data(data):
    status_dict = {}

    if not data:
        return status_dict

    for item in data:
        status = (
            item.get("status")
            or item.get("leadStatus")
            or item.get("newStatus")
            or item.get("name")
        )

        count = (
            item.get("count")
            or item.get("total")
            or item.get("quantity")
            or item.get("value")
            or 0
        )

        if status:
            status_dict[str(status).strip().lower()] = int(count)

    return status_dict


def get_dashboard_context():
    raw_status = call_api("/lead-status")
    status_data = normalize_status_data(raw_status)

    total = sum(status_data.values())

    won = status_data.get("won", 0)
    lost = status_data.get("lost", 0)

    won_rate = round((won / total) * 100, 2) if total > 0 else 0
    lost_rate = round((lost / total) * 100, 2) if total > 0 else 0

    open_leads = total - won - lost
    open_rate = round((open_leads / total) * 100, 2) if total > 0 else 0

    return {
        "status_data": status_data,
        "total": total,
        "new": status_data.get("new", 0),
        "connected": status_data.get("connected", 0),
        "qualified": status_data.get("qualified", 0),
        "proposal": status_data.get("proposal sent", 0),
        "negotiation": status_data.get("in negotiation", 0),
        "won": won,
        "lost": lost,
        "won_rate": won_rate,
        "lost_rate": lost_rate,
        "open_leads": open_leads,
        "open_rate": open_rate,
        "now": datetime.now().strftime("%d/%m/%Y %H:%M")
    }


def make_ai_summary(ctx):
    if ctx["total"] == 0:
        return "Hiện tại chưa có dữ liệu lead để phân tích."

    return (
        f"Tính đến {ctx['now']}, hệ thống đang có {ctx['total']} lead.\n"
        f"Trong đó có {ctx['won']} lead Won, {ctx['lost']} lead Lost "
        f"và {ctx['open_leads']} lead vẫn đang mở.\n"
        f"Tỉ lệ Won hiện tại là {ctx['won_rate']}%, "
        f"tỉ lệ Lost là {ctx['lost_rate']}%."
    )


def format_status_table(ctx):
    text = "Chi tiết số lead theo từng trạng thái:\n\n"

    for status, count in ctx["status_data"].items():
        rate = round((count / ctx["total"]) * 100, 2) if ctx["total"] > 0 else 0
        text += f"- {status.title()}: {count} lead ({rate}%)\n"

    text += (
        f"\nTổng cộng: {ctx['total']} lead.\n"
        f"Won: {ctx['won']} lead ({ctx['won_rate']}%).\n"
        f"Lost: {ctx['lost']} lead ({ctx['lost_rate']}%).\n"
        f"Lead đang mở: {ctx['open_leads']} lead ({ctx['open_rate']}%)."
    )

    return text


def answer_status_count(ctx, status_name, display_name):
    count = ctx["status_data"].get(status_name.lower(), 0)
    rate = round((count / ctx["total"]) * 100, 2) if ctx["total"] > 0 else 0

    return (
        f"Hiện tại có {count} lead đang ở trạng thái {display_name}.\n"
        f"Trạng thái này chiếm {rate}% trên tổng số {ctx['total']} lead."
    )


# =========================
# LEAD SOURCE
# =========================

def get_lead_source_data():
    data = call_api("/lead-source-cost")

    if data is None:
        return []

    result = []

    for item in data:
        lead_source = (
            item.get("leadSource")
            or item.get("source")
            or item.get("sourceName")
            or item.get("name")
            or "Không xác định"
        )

        total_leads = float(
            item.get("totalLeads")
            or item.get("leadCount")
            or item.get("totalLead")
            or 0
        )

        won_leads = float(
            item.get("wonLead")
            or item.get("wonLeads")
            or 0
        )

        revenue = float(
            item.get("revenue")
            or item.get("totalRevenue")
            or 0
        )

        cost = float(
            item.get("totalCost")
            or item.get("cost")
            or 0
        )

        cost_per_lead = float(
            item.get("costPerLead")
            or 0
        )

        conversion_rate = float(
            item.get("conversionRate")
            or 0
        )

        roi = float(
            item.get("roi")
            or 0
        )

        cost_per_win = float(
            item.get("costPerWin")
            or 0
        )

        result.append({
            "leadSource": lead_source,
            "totalLeads": total_leads,
            "wonLeads": won_leads,
            "revenue": revenue,
            "cost": cost,
            "conversionRate": conversion_rate,
            "roi": roi,
            "costPerLead": cost_per_lead,
            "costPerWin": cost_per_win
        })

    return result


def analyze_lead_source(question):
    q = question.lower()
    data = get_lead_source_data()

    if not data:
        return "Không lấy được dữ liệu Lead Source từ API /lead-source-cost."

    if "mang lại nhiều khách hàng" in q or "nhiều khách hàng nhất" in q or "nhiều lead nhất" in q:
        best = max(data, key=lambda x: x["totalLeads"])

        return (
            f"Nguồn lead mang lại nhiều khách hàng nhất là {best['leadSource']}.\n"
            f"Số lượng lead: {best['totalLeads']:,.0f} lead.\n"
            f"Số lead Won: {best['wonLeads']:,.0f} lead.\n"
            f"Conversion Rate: {best['conversionRate']}%."
        )

    if "conversion rate cao nhất" in q or "tỉ lệ chuyển đổi cao nhất" in q:
        best = max(data, key=lambda x: x["conversionRate"])

        return (
            f"Nguồn lead có Conversion Rate cao nhất là {best['leadSource']}.\n"
            f"Conversion Rate: {best['conversionRate']}%.\n"
            f"Số lead Won: {best['wonLeads']:,.0f}/{best['totalLeads']:,.0f} lead.\n\n"
            f"Nhận xét: Nguồn này có chất lượng chuyển đổi tốt nhất."
        )

    if "doanh thu" in q:
        best = max(data, key=lambda x: x["revenue"])

        return (
            f"Lead Source tạo ra nhiều doanh thu nhất là {best['leadSource']}.\n"
            f"Doanh thu: {best['revenue']:,.0f} VNĐ.\n"
            f"Chi phí: {best['cost']:,.0f} VNĐ.\n"
            f"ROI: {best['roi']}%."
        )

    if "roi" in q:
        best = max(data, key=lambda x: x["roi"])

        return (
            f"Lead Source có ROI cao nhất là {best['leadSource']}.\n"
            f"ROI: {best['roi']}%.\n"
            f"Doanh thu: {best['revenue']:,.0f} VNĐ.\n"
            f"Chi phí: {best['cost']:,.0f} VNĐ."
        )

    if "cost per lead" in q or "cpl" in q:
        answer = "Cost Per Lead theo từng nguồn:\n\n"

        for item in data:
            answer += (
                f"- {item['leadSource']}: {item['costPerLead']:,.0f} VNĐ/lead "
                f"(Chi phí {item['cost']:,.0f} VNĐ / {item['totalLeads']:,.0f} lead)\n"
            )

        return answer

    if "cost per win" in q or "cpw" in q:
        answer = "Cost Per Win theo từng nguồn:\n\n"

        for item in data:
            answer += (
                f"- {item['leadSource']}: {item['costPerWin']:,.0f} VNĐ/win "
                f"(Chi phí {item['cost']:,.0f} VNĐ / {item['wonLeads']:,.0f} won)\n"
            )

        return answer

    return "Tôi chưa hiểu câu hỏi về Lead Source."


def analyze_lead_source_by_intent(intent):
    data = get_lead_source_data()

    if not data:
        return "Không lấy được dữ liệu Lead Source từ API /lead-source-cost."

    if intent == "best_source_leads":
        best = max(data, key=lambda x: x["totalLeads"])
        return (
            f"Nguồn lead mang lại nhiều lead nhất là {best['leadSource']}.\n"
            f"Số lượng lead: {best['totalLeads']:,.0f} lead.\n"
            f"Số lead Won: {best['wonLeads']:,.0f} lead.\n"
            f"Conversion Rate: {best['conversionRate']}%."
        )

    if intent == "best_source_conversion":
        best = max(data, key=lambda x: x["conversionRate"])
        return (
            f"Nguồn có tỉ lệ chuyển đổi tốt nhất là {best['leadSource']}.\n"
            f"Conversion Rate: {best['conversionRate']}%.\n"
            f"Số lead Won: {best['wonLeads']:,.0f}/{best['totalLeads']:,.0f} lead.\n"
            f"Nhận xét: Đây là nguồn có chất lượng lead tốt nhất."
        )

    if intent == "best_source_revenue":
        best = max(data, key=lambda x: x["revenue"])
        return (
            f"Nguồn tạo doanh thu cao nhất là {best['leadSource']}.\n"
            f"Doanh thu: {best['revenue']:,.0f} VNĐ.\n"
            f"Chi phí: {best['cost']:,.0f} VNĐ.\n"
            f"ROI: {best['roi']}%."
        )

    if intent == "best_source_roi":
        best = max(data, key=lambda x: x["roi"])
        return (
            f"Nguồn có ROI cao nhất là {best['leadSource']}.\n"
            f"ROI: {best['roi']}%.\n"
            f"Doanh thu: {best['revenue']:,.0f} VNĐ.\n"
            f"Chi phí: {best['cost']:,.0f} VNĐ."
        )

    if intent == "source_cpl":
        answer = "Cost Per Lead theo từng nguồn:\n\n"
        for item in data:
            answer += f"- {item['leadSource']}: {item['costPerLead']:,.0f} VNĐ/lead\n"
        return answer

    if intent == "source_cpw":
        answer = "Cost Per Win theo từng nguồn:\n\n"
        for item in data:
            answer += f"- {item['leadSource']}: {item['costPerWin']:,.0f} VNĐ/win\n"
        return answer

    return "Tôi chưa hiểu câu hỏi về Lead Source."


def analyze_sales_owner_by_intent(intent):
    data = get_sales_owner_data()

    if not data:
        return "Không lấy được dữ liệu Sales Owner từ API /sales-owner-dashboard."

    if intent == "revenue_seller_bottom5":
        # Sắp xếp tăng dần (reverse=False) để lấy 5 người thấp nhất
        bottom5 = sorted(data, key=lambda x: x["totalRevenue"], reverse=False)[:5]
        answer = "Top 5 seller có doanh thu thấp nhất:\n\n"
        for i, item in enumerate(bottom5, start=1):
            answer += f"{i}. {item['userName']} - {item['totalRevenue']:,.0f} VNĐ, Won: {item['wonLead']:.0f}\n"
        return answer
    
    
    if intent == "best_seller_revenue":
        best = max(data, key=lambda x: x["totalRevenue"])
        return (
            f"Seller đang có doanh thu cao nhất là {best['userName']}.\n"
            f"Doanh thu: {best['totalRevenue']:,.0f} VNĐ.\n"
            f"Số lead Won: {best['wonLead']:,.0f} lead.\n"
            f"Win Rate: {best['winRate']}%."
        )
    
    if intent == "top_5_seller_revenue":
        top5 = sorted(data, key=lambda x: x["totalRevenue"], reverse=True)[:5]
        answer = "Top 5 seller có doanh thu cao nhất:\n\n"

        for i, item in enumerate(top5, start=1):
            answer += (
                f"{i}. {item['userName']} - "
                f"{item['totalRevenue']:,.0f} VNĐ, "
                f"Won: {item['wonLead']:,.0f}, "
                f"Win Rate: {item['winRate']}%\n"
            )

        return answer

    if intent == "best_seller_win_rate":
        best = max(data, key=lambda x: x["winRate"])
        return (
            f"Seller có Win Rate cao nhất là {best['userName']}.\n"
            f"Win Rate: {best['winRate']}%.\n"
            f"Số lead Won: {best['wonLead']:,.0f}/{best['totalLead']:,.0f} lead.\n"
            f"Nhận xét: Seller này có khả năng chuyển đổi lead tốt nhất."
        )

    if intent == "seller_most_open_leads":
        best = max(data, key=lambda x: x["openLead"])
        return (
            f"Seller đang có nhiều lead open nhất là {best['userName']}.\n"
            f"Số lead open: {best['openLead']:,.0f} lead.\n"
            f"Tổng lead phụ trách: {best['totalLead']:,.0f} lead.\n"
            f"Nhận xét: Seller này đang có nhiều lead cần tiếp tục chăm sóc."
        )

    if intent == "seller_fastest":
        valid_data = [x for x in data if x["avgDaysToWon"] > 0]

        if not valid_data:
            return "Chưa có dữ liệu thời gian xử lý lead để tính seller xử lý nhanh nhất."

        best = min(valid_data, key=lambda x: x["avgDaysToWon"])
        return (
            f"Seller xử lý lead nhanh nhất là {best['userName']}.\n"
            f"Thời gian trung bình Won: {best['avgDaysToWon']} ngày.\n"
            f"Số lead Won: {best['wonLead']:,.0f} lead."
        )

    return "Tôi chưa hiểu câu hỏi về Sales Owner."

# =========================
# SALES OWNER
# =========================

def get_sales_owner_data():
    data = call_api("/sales-owner-dashboard")

    if data is None:
        return []

    result = []

    for item in data:
        result.append({
            "userName": item.get("userName") or item.get("salesOwner") or item.get("sellerName") or "Không xác định",
            "totalLead": float(item.get("totalLead") or 0),
            "wonLead": float(item.get("wonLead") or 0),
            "openLead": float(item.get("openLead") or 0),
            "totalRevenue": float(item.get("totalRevenue") or 0),
            "winRate": float(item.get("winRate") or 0),
            "avgDaysToWon": float(item.get("avgDaysToWon") or 0)
        })

    return result


def analyze_sales_owner(question):
    q = question.lower()
    data = get_sales_owner_data()

    if not data:
        return "Không lấy được dữ liệu Sales Owner từ API /sales-owner-dashboard."

    if "doanh thu cao nhất" in q and "top 5" not in q:
        best = max(data, key=lambda x: x["totalRevenue"])

        return (
            f"Seller có doanh thu cao nhất là {best['userName']}.\n"
            f"Doanh thu: {best['totalRevenue']:,.0f} VNĐ.\n"
            f"Số lead Won: {best['wonLead']:,.0f} lead.\n"
            f"Win Rate: {best['winRate']}%."
        )

    if "top 5" in q and "doanh thu cao nhất" in q:
        top5 = sorted(data, key=lambda x: x["totalRevenue"], reverse=True)[:5]

        answer = "Top 5 seller có doanh thu cao nhất:\n\n"

        for i, item in enumerate(top5, start=1):
            answer += (
                f"{i}. {item['userName']} - "
                f"{item['totalRevenue']:,.0f} VNĐ, "
                f"Won: {item['wonLead']:,.0f}, "
                f"Win Rate: {item['winRate']}%\n"
            )

        return answer

    if "win rate cao nhất" in q:
        best = max(data, key=lambda x: x["winRate"])

        return (
            f"Seller có Win Rate cao nhất là {best['userName']}.\n"
            f"Win Rate: {best['winRate']}%.\n"
            f"Số lead Won: {best['wonLead']:,.0f}/{best['totalLead']:,.0f} lead.\n"
            f"Nhận xét: Đây là seller có khả năng chuyển đổi lead tốt nhất."
        )

    if "nhiều lead open" in q or "lead open nhiều nhất" in q or "đang có nhiều lead open" in q:
        best = max(data, key=lambda x: x["openLead"])

        return (
            f"Seller đang có nhiều lead open nhất là {best['userName']}.\n"
            f"Số lead open: {best['openLead']:,.0f} lead.\n"
            f"Tổng lead phụ trách: {best['totalLead']:,.0f} lead.\n"
            f"Nhận xét: Seller này đang có nhiều lead cần tiếp tục chăm sóc."
        )

    if "xử lý lead nhanh nhất" in q or "chốt nhanh nhất" in q or "nhanh nhất" in q:
        valid_data = [x for x in data if x["avgDaysToWon"] > 0]

        if not valid_data:
            return "Chưa có dữ liệu thời gian xử lý lead để tính seller xử lý nhanh nhất."

        best = min(valid_data, key=lambda x: x["avgDaysToWon"])

        return (
            f"Seller xử lý lead nhanh nhất là {best['userName']}.\n"
            f"Thời gian trung bình Won: {best['avgDaysToWon']} ngày.\n"
            f"Số lead Won: {best['wonLead']:,.0f} lead.\n"
            f"Nhận xét: Seller này có tốc độ chuyển đổi lead thành khách hàng nhanh nhất."
        )

    return "Tôi chưa hiểu câu hỏi về Sales Owner."
# =========================
# PIPELINE COVERAGE
# =========================

def get_pipeline_data():
    data = call_api("/pipeline-coverage")

    if data is None:
        return []

    result = []

    for item in data:
        result.append({
            "sellerName": item.get("salesOwner") or item.get("sellerName") or item.get("userName") or "Không xác định",
            "openPipeline": float(item.get("openPipeline") or 0),
            "target": float(item.get("target") or item.get("targetRevenue") or 0),
            "pipelineCoverage": float(item.get("pipelineCoverage") or 0),
            "periodMonth": item.get("periodMonth") # Lấy thêm trường tháng để phân tích lịch sử
        })

    return result


def analyze_pipeline_by_intent(intent):
    data = get_pipeline_data()

    if not data:
        return "Không lấy được dữ liệu Pipeline Coverage từ API /pipeline-coverage."

    total_pipeline = sum(x["openPipeline"] for x in data)
    total_target = sum(x["target"] for x in data)
    total_coverage = round(total_pipeline / total_target, 2) if total_target > 0 else 0

    if intent == "pipeline_current":
        return (
            f"Pipeline Coverage hiện tại là {total_coverage} lần.\n"
            f"Tổng open pipeline: {total_pipeline:,.0f} VNĐ.\n"
            f"Tổng target: {total_target:,.0f} VNĐ."
        )

    if intent == "pipeline_best_seller":
        best = max(data, key=lambda x: x["pipelineCoverage"])

        return (
            f"Seller có Pipeline Coverage cao nhất là {best['sellerName']}.\n"
            f"Pipeline Coverage: {best['pipelineCoverage']} lần.\n"
            f"Open Pipeline: {best['openPipeline']:,.0f} VNĐ.\n"
            f"Target: {best['target']:,.0f} VNĐ."
        )

    if intent == "pipeline_enough_target":
        if total_coverage >= 3:
            comment = "khá tốt, có khả năng hỗ trợ đạt target."
        elif total_coverage >= 1:
            comment = "tạm đủ nhưng cần theo dõi khả năng chuyển đổi."
        else:
            comment = "chưa đủ mạnh để đạt target."

        return (
            f"Pipeline hiện tại đạt {total_coverage} lần so với target.\n"
            f"Kết luận: Pipeline {comment}\n"
            f"Tổng open pipeline: {total_pipeline:,.0f} VNĐ.\n"
            f"Tổng target: {total_target:,.0f} VNĐ."
        )

    return "Tôi chưa hiểu câu hỏi về Pipeline Coverage."

# =========================
# LOST ANALYSIS
# =========================

def get_lost_reasons_data():
    data = call_api("/lost-reasons")
    if data is None:
        return []

    result = []
    for item in data:
        result.append({
            "reason": item.get("reason") or item.get("lossReason") or item.get("lostReason") or "Không xác định",
            "lostLead": float(item.get("lostLead") or item.get("totalLost") or item.get("count") or item.get("total") or 0),
            "lostRate": float(item.get("lostRate") or item.get("rate") or 0)
        })

    return result


def get_lost_by_seller_data():
    data = call_api("/lost-by-seller")
    if data is None:
        return []

    result = []
    for item in data:
        result.append({
            "userCode": item.get("userCode") or "",
            "salesOwner": item.get("salesOwner") or item.get("sellerName") or item.get("userName") or "Không xác định",
            "lostLead": float(item.get("lostLead") or 0),
            "lostRate": float(item.get("lostRate") or 0)
        })

    return result


def get_lost_by_source_data():
    data = call_api("/lost-by-source")
    if data is None:
        return []

    result = []
    for item in data:
        result.append({
            "sourceId": item.get("sourceId") or "",
            "sourceName": item.get("sourceName") or item.get("leadSource") or "Không xác định",
            "lostLead": float(item.get("lostLead") or 0),
            "lostRate": float(item.get("lostRate") or 0)
        })

    return result


def get_lost_by_region_data():
    data = call_api("/lost-by-region")
    if data is None:
        return []

    result = []
    for item in data:
        result.append({
            "region": item.get("region") or "Không xác định",
            "lostLead": float(item.get("lostLead") or 0),
            "lostRate": float(item.get("lostRate") or 0)
        })

    return result


def get_lost_by_industry_data():
    data = call_api("/lost-by-industry")
    if data is None:
        return []

    result = []
    for item in data:
        result.append({
            "industryType": item.get("industryType") or item.get("industry") or "Không xác định",
            "lostLead": float(item.get("lostLead") or 0),
            "lostRate": float(item.get("lostRate") or 0)
        })

    return result

def analyze_lost_by_intent(intent, question):
    q, q2 = normalize_question(question)

    reasons = get_lost_reasons_data()

    if intent in [
        "lost_reason_most_common",
        "lost_reason_price",
        "lost_reason_list",
        "lost_reason_highest_rate",
        "top_5_lost_reasons",
        "total_lost",
        "lost_rate_current"
    ]:
        if not reasons:
            return "Không lấy được dữ liệu Lost Reason từ API /lost-reasons."

    # SỬA LỖI CHÍNH TẢ: Thay đổi reasongs -> reasons
    total_lost = sum(x["lostLead"] for x in reasons) if reasons else 0

    if intent == "lost_reason_most_common":
        best = max(reasons, key=lambda x: x["lostLead"])

        return (
            f"Lý do Lost phổ biến nhất là: {best['reason']}.\n"
            f"Số lead Lost: {best['lostLead']:,.0f} lead.\n"
            f"Tỉ lệ trong nhóm Lost: {best['lostRate']}%."
        )

    if intent == "lost_reason_price":
        matched = [
            x for x in reasons
            if "giá" in x["reason"].lower()
            or "gia" in remove_accents(x["reason"].lower())
            or "price" in x["reason"].lower()
        ]

        if not matched:
            return "Không tìm thấy lý do Lost liên quan đến giá trong dữ liệu."

        total_price_lost = sum(x["lostLead"] for x in matched)

        return (
            f"Có {total_price_lost:,.0f} lead bị Lost vì lý do liên quan đến giá.\n"
            f"Chi tiết:\n" +
            "".join([f"- {x['reason']}: {x['lostLead']:,.0f} lead\n" for x in matched])
        )

    if intent == "total_lost":
        return f"Tổng số lead Lost hiện tại là {total_lost:,.0f} lead."

    if intent == "lost_rate_current":
        ctx = get_dashboard_context()
        if ctx["total"] == 0:
            return "Không tính được tỉ lệ Lost vì không có dữ liệu tổng lead."

        return (
            f"Tỉ lệ Lost hiện tại là {ctx['lost_rate']}%.\n"
            f"Cách tính: {ctx['lost']} lead Lost / {ctx['total']} tổng lead x 100."
        )

    if intent == "lost_reason_list":
        answer = "Danh sách lý do Lost và số lượng từng lý do:\n\n"

        for item in sorted(reasons, key=lambda x: x["lostLead"], reverse=True):
            answer += (
                f"- {item['reason']}: {item['lostLead']:,.0f} lead "
                f"({item['lostRate']}%)\n"
            )

        return answer

    if intent == "lost_reason_highest_rate":
        best = max(reasons, key=lambda x: x["lostRate"])

        return (
            f"Lý do Lost chiếm tỉ lệ cao nhất là: {best['reason']}.\n"
            f"Tỉ lệ: {best['lostRate']}%.\n"
            f"Số lead Lost: {best['lostLead']:,.0f} lead."
        )

    if intent == "top_5_lost_reasons":
        top5 = sorted(reasons, key=lambda x: x["lostLead"], reverse=True)[:5]

        answer = "Top 5 lý do Lost phổ biến nhất:\n\n"
        for i, item in enumerate(top5, start=1):
            answer += (
                f"{i}. {item['reason']} - "
                f"{item['lostLead']:,.0f} lead "
                f"({item['lostRate']}%)\n"
            )

        return answer

    if intent == "lost_seller_highest_rate":
        data = get_lost_by_seller_data()
        if not data:
            return "Không lấy được dữ liệu Lost theo Seller từ API /lost-by-seller."

        best = max(data, key=lambda x: x["lostRate"])

        return (
            f"Seller có tỉ lệ Lost cao nhất là {best['salesOwner']}.\n"
            f"Tỉ lệ Lost: {best['lostRate']}%.\n"
            f"Số lead Lost: {best['lostLead']:,.0f} lead."
        )

    if intent == "lost_seller_most_count":
        data = get_lost_by_seller_data()
        if not data:
            return "Không lấy được dữ liệu Lost theo Seller từ API /lost-by-seller."

        best = max(data, key=lambda x: x["lostLead"])

        return (
            f"Seller có nhiều lead Lost nhất là {best['salesOwner']}.\n"
            f"Số lead Lost: {best['lostLead']:,.0f} lead.\n"
            f"Tỉ lệ Lost: {best['lostRate']}%."
        )

    if intent == "lost_source_most":
        data = get_lost_by_source_data()
        if not data:
            return "Không lấy được dữ liệu Lost theo Source từ API /lost-by-source."

        best = max(data, key=lambda x: x["lostLead"])

        return (
            f"Nguồn lead có nhiều Lost nhất là {best['sourceName']}.\n"
            f"Số lead Lost: {best['lostLead']:,.0f} lead.\n"
            f"Tỉ lệ Lost: {best['lostRate']}%."
        )

    if intent == "lost_region_most":
        data = get_lost_by_region_data()
        if not data:
            return "Không lấy được dữ liệu Lost theo Region từ API /lost-by-region."

        best = max(data, key=lambda x: x["lostLead"])

        return (
            f"Region có nhiều Lost nhất là {best['region']}.\n"
            f"Số lead Lost: {best['lostLead']:,.0f} lead.\n"
            f"Tỉ lệ Lost: {best['lostRate']}%."
        )

    if intent == "lost_industry_most":
        data = get_lost_by_industry_data()
        if not data:
            return "Không lấy được dữ liệu Lost theo Industry từ API /lost-by-industry."

        best = max(data, key=lambda x: x["lostLead"])

        return (
            f"Industry có nhiều Lost nhất là {best['industryType']}.\n"
            f"Số lead Lost: {best['lostLead']:,.0f} lead.\n"
            f"Tỉ lệ Lost: {best['lostRate']}%."
        )

    return "Tôi chưa hiểu câu hỏi Lost Analysis."

# =========================
# REVENUE ANALYSIS
# =========================

def get_revenue_summary_data():
    data = call_api("/revenue-summary")
    if data is None:
        return {}

    return {
        "totalRevenue": float(data.get("totalRevenue") or 0),
        "wonLead": float(data.get("wonLead") or 0),
        "avgRevenuePerWonLead": float(data.get("avgRevenuePerWonLead") or 0),
        "thisMonthRevenue": float(data.get("thisMonthRevenue") or 0),
        "lastMonthRevenue": float(data.get("lastMonthRevenue") or 0)
    }


def get_revenue_lead_source_data():
    data = call_api("/lead-source-cost")
    if data is None:
        return []

    result = []

    for item in data:
        result.append({
            "name": item.get("sourceName") or item.get("leadSource") or "Không xác định",
            "revenue": float(item.get("revenue") or item.get("totalRevenue") or 0),
            "wonLead": float(item.get("wonLead") or item.get("wonLeads") or 0),
            "avgRevenue": (
                float(item.get("revenue") or item.get("totalRevenue") or 0)
                / float(item.get("wonLead") or item.get("wonLeads") or 1)
            )
        })

    return result


def get_revenue_product_line_data():
    data = call_api("/revenue-product-line")
    if data is None:
        return []

    result = []

    for item in data:
        revenue = float(item.get("revenue") or item.get("totalRevenue") or 0)
        won = float(item.get("totalWonLead") or item.get("wonLead") or 0)

        result.append({
            "name": item.get("productName") or item.get("productLine") or "Không xác định",
            "revenue": revenue,
            "wonLead": won,
            "avgRevenue": round(revenue / won, 2) if won > 0 else 0
        })

    return result


def get_revenue_region_data():
    data = call_api("/revenue-region")
    if data is None:
        return []

    result = []

    for item in data:
        revenue = float(item.get("revenue") or item.get("totalRevenue") or 0)
        won = float(item.get("wonLead") or item.get("totalWonLead") or 0)

        result.append({
            "name": item.get("region") or "Không xác định",
            "revenue": revenue,
            "wonLead": won,
            "avgRevenue": round(revenue / won, 2) if won > 0 else 0
        })

    return result


def get_revenue_industry_data():
    data = call_api("/revenue-industry")
    if data is None:
        return []

    result = []

    for item in data:
        revenue = float(item.get("revenue") or item.get("totalRevenue") or 0)
        won = float(item.get("wonLead") or item.get("totalWonLead") or 0)

        result.append({
            "name": item.get("industryType") or item.get("industry") or "Không xác định",
            "revenue": revenue,
            "wonLead": won,
            "avgRevenue": round(revenue / won, 2) if won > 0 else 0
        })

    return result


def get_revenue_seller_data():
    data = get_sales_owner_data()

    result = []

    for item in data:
        revenue = float(item.get("totalRevenue") or 0)
        won = float(item.get("wonLead") or 0)

        result.append({
            "name": item.get("userName") or "Không xác định",
            "revenue": revenue,
            "wonLead": won,
            "avgRevenue": round(revenue / won, 2) if won > 0 else 0
        })

    return result
def format_revenue_list(title, data):
    if not data:
        return "Không có dữ liệu doanh thu."

    total = sum(x["revenue"] for x in data)

    answer = f"{title}\n\n"

    for i, item in enumerate(sorted(data, key=lambda x: x["revenue"], reverse=True), start=1):
        rate = round((item["revenue"] / total) * 100, 2) if total > 0 else 0
        answer += (
            f"{i}. {item['name']}: {item['revenue']:,.0f} VNĐ "
            f"({rate}%)"
        )

        if item.get("wonLead", 0) > 0:
            answer += f" - Won: {item['wonLead']:,.0f}, TB/Won: {item['avgRevenue']:,.0f} VNĐ"

        answer += "\n"

    answer += f"\nTổng doanh thu: {total:,.0f} VNĐ."
    return answer


def answer_best_revenue(label, data):
    if not data:
        return f"Không có dữ liệu doanh thu theo {label}."

    best = max(data, key=lambda x: x["revenue"])
    total = sum(x["revenue"] for x in data)
    rate = round((best["revenue"] / total) * 100, 2) if total > 0 else 0

    group = "revenue"
    if label == "Lead Source":
        group = "source"
    elif label == "Seller":
        group = "seller"
    elif label == "Product Line":
        group = "product"
    elif label == "Region":
        group = "region"
    elif label == "Industry":
        group = "industry"

    return build_ai_answer(
        title=f"{label} có doanh thu cao nhất là {best['name']}.",
        result=(
            f"- Doanh thu: {best['revenue']:,.0f} VNĐ\n"
            f"- Số lead Won: {best['wonLead']:,.0f}\n"
            f"- Doanh thu trung bình mỗi lead Won: {best['avgRevenue']:,.0f} VNĐ\n"
            f"- Đóng góp: {rate}% tổng doanh thu nhóm {label}"
        ),
        insight=(
            f"{best['name']} đang là nhóm đóng góp doanh thu mạnh nhất trong {label}. "
            f"Nên xem thêm nguyên nhân tạo doanh thu cao: chất lượng lead, seller phụ trách, "
            f"hoặc sản phẩm/nhóm khách hàng liên quan."
        ),
        follow_group=group
    )

def answer_lowest_revenue(label, data):
    if not data:
        return f"Không có dữ liệu doanh thu theo {label}."

    valid = [x for x in data if x["revenue"] > 0]

    if not valid:
        return f"Chưa có {label} nào phát sinh doanh thu lớn hơn 0."

    low = min(valid, key=lambda x: x["revenue"])
    total = sum(x["revenue"] for x in data)
    rate = round((low["revenue"] / total) * 100, 2) if total > 0 else 0

    return build_ai_answer(
        title=f"{label} có doanh thu thấp nhất là {low['name']}.",
        result=(
            f"- Doanh thu: {low['revenue']:,.0f} VNĐ\n"
            f"- Số lead Won: {low['wonLead']:,.0f}\n"
            f"- Doanh thu trung bình mỗi lead Won: {low['avgRevenue']:,.0f} VNĐ\n"
            f"- Tỉ trọng trong nhóm: {rate}%"
        ),
        insight=(
            f"{low['name']} đang đóng góp doanh thu thấp nhất. "
            f"Nên kiểm tra thêm số lượng lead, tỉ lệ Won, lý do Lost và chất lượng nguồn lead."
        ),
        follow_group="revenue"
    )


def answer_top5_revenue(label, data):
    if not data:
        return f"Không có dữ liệu doanh thu theo {label}."

    top5 = sorted(data, key=lambda x: x["revenue"], reverse=True)[:5]
    total = sum(x["revenue"] for x in data)

    result = ""

    for i, item in enumerate(top5, start=1):
        rate = round((item["revenue"] / total) * 100, 2) if total > 0 else 0
        result += (
            f"{i}. {item['name']}\n"
            f"    - Doanh thu: {item['revenue']:,.0f} VNĐ\n"
            f"    - Won: {item['wonLead']:,.0f}\n"
            f"    - TB/Won: {item['avgRevenue']:,.0f} VNĐ\n"
            f"    - Tỉ trọng: {rate}%\n"
        )

    return build_ai_answer(
        title=f"Top 5 {label} có doanh thu cao nhất.",
        result=result,
        insight=(
            f"Nhóm top 5 này đang là phần đóng góp chính cho doanh thu. "
            f"Admin nên ưu tiên phân tích sâu các nhóm này để biết vì sao hiệu quả cao."
        ),
        follow_group="revenue"
    )

def answer_avg_revenue(label, data):
    if not data:
        return f"Không có dữ liệu doanh thu theo {label}."

    answer = f"Doanh thu trung bình theo {label}:\n\n"

    for item in sorted(data, key=lambda x: x["avgRevenue"], reverse=True):
        answer += (
            f"- {item['name']}: {item['avgRevenue']:,.0f} VNĐ/Won "
            f"({item['revenue']:,.0f} VNĐ / {item['wonLead']:,.0f} Won)\n"
        )

    return answer
def build_follow_up(group):
    suggestions = {
        "revenue": [
            "Doanh thu theo Lead Source như thế nào?",
            "Region nào có doanh thu cao nhất?",
            "Top 5 seller doanh thu cao nhất?"
        ],
        "source": [
            "Lead Source nào tạo doanh thu cao nhất?",
            "ROI theo từng Lead Source như thế nào?",
            "Cost Per Win theo Lead Source là bao nhiêu?"
        ],
        "seller": [
            "Seller nào có doanh thu cao nhất?",
            "Top 5 seller doanh thu thấp nhất?",
            "Doanh thu trung bình theo Seller?"
        ],
        "product": [
            "Product Line nào tạo doanh thu cao nhất?",
            "Top 5 Product Line doanh thu cao nhất?",
            "Doanh thu trung bình theo Product Line?"
        ],
        "region": [
            "Region nào có doanh thu thấp nhất?",
            "Doanh thu trung bình theo Region?",
            "Doanh thu theo Industry như thế nào?"
        ],
        "industry": [
            "Industry nào có doanh thu cao nhất?",
            "Industry nào có doanh thu thấp nhất?",
            "Doanh thu trung bình theo Industry?"
        ]
    }

    items = suggestions.get(group, suggestions["revenue"])

    text = "\n\nBạn có thể hỏi tiếp:\n"
    for item in items:
        text += f"• {item}\n"

    return text


def build_ai_answer(title, result, insight, follow_group="revenue"):
    return (
        f"{title}\n\n"
        f"📊 Kết quả:\n"
        f"{result}\n\n"
        f"💡 Nhận xét:\n"
        f"{insight}"
        f"{build_follow_up(follow_group)}"
    )


def revenue_health_comment(revenue):
    if revenue <= 0:
        return "Hiện chưa ghi nhận doanh thu Won, cần kiểm tra lại dữ liệu hoặc tình trạng chốt đơn."
    if revenue >= 10_000_000_000:
        return "Doanh thu đang ở mức rất tốt, nên tiếp tục phân tích nguồn nào và seller nào đang đóng góp chính."
    if revenue >= 1_000_000_000:
        return "Doanh thu đang ở mức khá ổn, có thể tiếp tục tối ưu các nguồn lead và seller có hiệu suất cao."
    return "Doanh thu còn thấp, nên xem thêm lý do Lost, nguồn lead kém hiệu quả và pipeline hiện tại."
def analyze_revenue_by_intent(intent, question):
    if intent in ["revenue_current", "revenue_total_won", "revenue_avg_per_won", "revenue_this_month", "revenue_last_month", "revenue_month_compare"]:
        summary = get_revenue_summary_data()

        if not summary:
            return "Không lấy được dữ liệu doanh thu từ API /revenue-summary."

        if intent in ["revenue_current", "revenue_total_won"]:
            return build_ai_answer(
        title="Tổng quan doanh thu Won hiện tại.",
        result=(
            f"- Tổng doanh thu Won: {summary['totalRevenue']:,.0f} VNĐ\n"
            f"- Số lead Won: {summary['wonLead']:,.0f} lead\n"
            f"- Doanh thu trung bình mỗi lead Won: {summary['avgRevenuePerWonLead']:,.0f} VNĐ"
        ),
        insight=revenue_health_comment(summary["totalRevenue"]),
        follow_group="revenue"
    )

        if intent == "revenue_avg_per_won":
            return (
                f"Doanh thu trung bình trên mỗi lead Won là "
                f"{summary['avgRevenuePerWonLead']:,.0f} VNĐ."
            )

        if intent == "revenue_this_month":
            return f"Doanh thu tháng này là {summary['thisMonthRevenue']:,.0f} VNĐ."

        if intent == "revenue_last_month":
            return f"Doanh thu tháng trước là {summary['lastMonthRevenue']:,.0f} VNĐ."

        if intent == "revenue_month_compare":
            this_month = summary["thisMonthRevenue"]
            last_month = summary["lastMonthRevenue"]

            diff = this_month - last_month
            rate = round((diff / last_month) * 100, 2) if last_month > 0 else 0

            if diff > 0:
                trend = "tăng"
            elif diff < 0:
                trend = "giảm"
            else:
                trend = "không thay đổi"

            return (
                f"Doanh thu tháng này {trend} so với tháng trước.\n"
                f"Tháng này: {this_month:,.0f} VNĐ.\n"
                f"Tháng trước: {last_month:,.0f} VNĐ.\n"
                f"Chênh lệch: {diff:,.0f} VNĐ ({rate}%)."
            )

    if intent.startswith("revenue_source"):
        data = get_revenue_lead_source_data()
        label = "Lead Source"

        if intent == "revenue_source_list":
            return format_revenue_list("Doanh thu theo Lead Source:", data)
        if intent == "revenue_source_best":
            return answer_best_revenue(label, data)
        if intent == "revenue_source_lowest":
            return answer_lowest_revenue(label, data)
        if intent == "revenue_source_top5":
            return answer_top5_revenue(label, data)
        if intent == "revenue_source_avg":
            return answer_avg_revenue(label, data)

    if intent.startswith("revenue_product"):
        data = get_revenue_product_line_data()
        label = "Product Line"

        if intent == "revenue_product_list":
            return format_revenue_list("Doanh thu theo Product Line:", data)
        if intent == "revenue_product_best":
            return answer_best_revenue(label, data)
        if intent == "revenue_product_lowest":
            return answer_lowest_revenue(label, data)
        if intent == "revenue_product_top5":
            return answer_top5_revenue(label, data)
        if intent == "revenue_product_avg":
            return answer_avg_revenue(label, data)

    if intent.startswith("revenue_region"):
        data = get_revenue_region_data()
        label = "Region"

        if intent == "revenue_region_list":
            return format_revenue_list("Doanh thu theo Region:", data)
        if intent == "revenue_region_best":
            return answer_best_revenue(label, data)
        if intent == "revenue_region_lowest":
            return answer_lowest_revenue(label, data)
        if intent == "revenue_region_avg":
            return answer_avg_revenue(label, data)

    if intent.startswith("revenue_industry"):
        data = get_revenue_industry_data()
        label = "Industry"

        if intent == "revenue_industry_list":
            return format_revenue_list("Doanh thu theo Industry:", data)
        if intent == "revenue_industry_best":
            return answer_best_revenue(label, data)
        if intent == "revenue_industry_lowest":
            return answer_lowest_revenue(label, data)
        if intent == "revenue_industry_avg":
            return answer_avg_revenue(label, data)

    if intent.startswith("revenue_seller"):
        data = get_revenue_seller_data()
        label = "Seller"

        if intent == "revenue_seller_list":
            return format_revenue_list("Doanh thu theo Seller:", data)
        if intent == "revenue_seller_best":
            return answer_best_revenue(label, data)
        if intent == "revenue_seller_lowest":
            return answer_lowest_revenue(label, data)
        if intent == "revenue_seller_top5":
            return answer_top5_revenue(label, data)
        if intent == "revenue_seller_bottom5":
            bottom5 = sorted(data, key=lambda x: x["revenue"])[:5]
            answer = "Top 5 seller có doanh thu thấp nhất:\n\n"
            for i, item in enumerate(bottom5, start=1):
                answer += (
                    f"{i}. {item['name']} - {item['revenue']:,.0f} VNĐ, "
                    f"Won: {item['wonLead']:,.0f}, "
                    f"TB/Won: {item['avgRevenue']:,.0f} VNĐ\n"
                )
            return answer
        if intent == "revenue_seller_avg":
            return answer_avg_revenue(label, data)

    return "Tôi chưa hiểu câu hỏi Revenue."

def detect_intent(question):
    q, q2 = normalize_question(question)

    # =====================================================
    # ÜU TIÊN SỐ 1: WHAT IF ANALYSIS & FORECAST_WORDS
    # (Phải check kịch bản giả định trước để tránh trùng từ khóa Doanh thu/Seller)
    # =====================================================
    if has_any(q2, ["neu", "gia su", "what if"]):
        if has_any(q2, ["win rate"]): return "what_if_win_rate"
        if has_any(q2, ["conversion"]): return "what_if_conversion"
        if has_any(q2, ["lead"]): return "what_if_lead"
        if has_any(q2, ["ngan sach", "budget"]): return "what_if_budget"
        if has_any(q2, ["seller", "sale", "sales owner", "nhan vien", "doanh so", "doanh thu"]): 
            return "what_if_seller"

    FORECAST_WORDS = [
        "du doan", "forecast", "uoc tinh", "du kien",
        "sap toi", "tuong lai", "thang toi", "thang sau",
        "quy toi", "quy sau", "nam toi", "nam sau"
    ]

    if has_any(q2, FORECAST_WORDS):
        # REVENUE FORECAST
        if has_any(q2, ["doanh thu", "revenue", "doanh so", "tien mang ve", "tien kiem duoc", "sales"]):
            if has_any(q2, ["thang sau", "thang toi", "next month"]): return "forecast_revenue_month"
            if has_any(q2, ["quy sau", "quy toi", "next quarter"]): return "forecast_revenue_quarter"
            if has_any(q2, ["nam sau", "nam toi", "2027", "next year"]): return "forecast_revenue_year"
            if has_any(q2, ["xu huong", "tinh hinh", "tang hay giam", "di len", "di xuong", "gan day", "trend"]): return "forecast_revenue_trend"
            if has_any(q2, ["tang truong", "growth", "bao nhieu phan tram", "%"]): return "forecast_revenue_growth"

        # LEAD FORECAST
        if has_any(q2, ["lead", "khach tiem nang", "khach moi"]):
            if has_any(q2, ["thang sau", "thang toi"]): return "forecast_lead_month"
            if has_any(q2, ["quy sau", "quy toi"]): return "forecast_lead_quarter"

        # WON FORECAST
        if has_any(q2, ["won", "deal thanh cong", "chot thanh cong", "khach mua"]):
            if has_any(q2, ["quy sau", "quy toi"]): return "forecast_won_quarter"

        # LOST FORECAST
        if has_any(q2, ["lost", "mat khach", "rot lead", "that bai"]):
            if has_any(q2, ["quy sau", "quy toi"]): return "forecast_lost_quarter"

        # RATES FORECAST
        if has_any(q2, ["conversion", "conversion rate", "ti le chuyen doi", "ty le chuyen doi"]): return "forecast_conversion"
        if has_any(q2, ["win rate", "ti le won", "ty le won", "ti le chot"]): return "forecast_win_rate"
        if has_any(q2, ["lost rate", "ti le lost", "ty le lost"]): return "forecast_lost_rate"

        # SELLER FORECAST
        if has_any(q2, ["seller", "sale", "sales owner", "nhan vien"]):
            if has_any(q2, ["top 5", "top seller", "xep hang"]): return "forecast_top5_seller"
            if has_any(q2, ["cao nhat", "tot nhat", "dan dau", "tiem nang nhat", "mang ve nhieu tien nhat"]): return "forecast_best_seller"
            if has_any(q2, ["giam doanh thu", "rui ro", "di xuong", "can ho tro"]): return "forecast_seller_risk"

        # SOURCE FORECAST
        if has_any(q2, ["source", "lead source", "nguon", "kenh"]):
            if has_any(q2, ["cao nhat", "tot nhat", "hieu qua nhat", "mang lai nhieu tien nhat"]): return "forecast_best_source"
            if has_any(q2, ["giam", "suy giam", "kem hieu qua", "cat ngan sach"]): return "forecast_source_risk"

        # REGION FORECAST
        if has_any(q2, ["region", "khu vuc", "vung", "mien"]):
            if has_any(q2, ["cao nhat", "tiem nang nhat", "tang truong nhat"]): return "forecast_best_region"
            if has_any(q2, ["giam", "suy giam", "rui ro"]): return "forecast_region_risk"

        # INDUSTRY FORECAST
        if has_any(q2, ["industry", "nganh", "linh vuc"]):
            if has_any(q2, ["cao nhat", "tot nhat", "tiem nang nhat"]): return "forecast_best_industry"
            if has_any(q2, ["giam", "suy giam", "rui ro"]): return "forecast_industry_risk"

        # PRODUCT FORECAST
        if has_any(q2, ["product", "product line", "san pham", "dong san pham"]):
            if has_any(q2, ["cao nhat", "tot nhat", "tiem nang nhat"]): return "forecast_best_product"
            if has_any(q2, ["giam", "suy giam", "can xem lai"]): return "forecast_product_risk"

        # TARGET / KPI
        if has_any(q2, ["target", "chi tieu", "kpi", "ke hoach"]):
            if has_any(q2, ["co dat", "hoan thanh", "dat duoc"]): return "forecast_target_achievable"
            if has_any(q2, ["xac suat", "bao nhieu phan tram"]): return "forecast_target_probability"
            if has_any(q2, ["con thieu", "gap"]): return "forecast_target_gap"
            if has_any(q2, ["can them doanh thu", "them bao nhieu tien"]): return "forecast_target_revenue_gap"
            if has_any(q2, ["can them lead", "them bao nhieu lead"]): return "forecast_target_lead_gap"

    # =====================================================
    # ÜU TIÊN SỐ 2: CÁC CÂU HỎI THỐNG KÊ HIỆN TẠI (DỮ LIỆU CŨ)
    # =====================================================
    
    # SALES OWNER
    seller_words = ["seller", "sale", "sales owner", "nhân viên", "nhan vien", "người bán", "nguoi ban", "sale nào", "sale nao", "ai bán", "ai ban", "ai chốt", "ai chot"]
    if has_any(q, seller_words) or has_any(q2, seller_words):
        
        if has_any(q2, ["top 5 doanh thu", "top seller", "xep hang seller", "danh sach seller doanh thu", "5 sale doanh thu"]): return "top_5_seller_revenue"
        if has_any(q2, ["doanh thu cao nhat", "ban tot nhat", "kiem tien nhieu nhat", "ai mang tien ve nhieu nhat", "ai doanh thu tot nhat", "sale nao tot nhat", "seller nao tot nhat", "nguoi nao ban duoc nhieu nhat"]): return "best_seller_revenue"
        if has_any(q2, ["win rate cao nhat", "ti le thang cao nhat", "ty le thang cao nhat", "chuyen doi tot nhat", "chot tot nhat", "ti le chot cao nhat", "ai chot thanh cong nhieu nhat"]): return "best_seller_win_rate"
        if has_any(q2, ["lead open nhieu nhat", "nhieu lead open", "dang co nhieu lead", "dang xu ly nhieu lead", "nhieu lead chua chot", "ai dang om nhieu lead", "seller nao ban ron nhat"]): return "seller_most_open_leads"
        if has_any(q2, ["xu ly nhanh nhat", "chot nhanh nhat", "it ngay nhat", "ai xu ly nhanh", "sale nao nhanh nhat", "thoi gian won nhanh nhat", "chot deal nhanh nhat"]): return "seller_fastest"

    # LEAD SOURCE
    source_words = ["nguồn", "nguon", "lead source", "source", "kênh", "kenh", "nguồn lead", "nguon lead", "kênh lead", "kenh lead"]
    if has_any(q, source_words) or has_any(q2, source_words):
        if has_any(q2, ["nhieu lead nhat", "mang lai nhieu khach", "nhieu khach hang nhat", "kenh nao do lead nhieu", "nguon nao co nhieu lead", "nguon nao mang ve nhieu lead"]): return "best_source_leads"
        if has_any(q2, ["conversion cao nhat", "conversion rate cao nhat", "ti le chuyen doi cao nhat", "ty le chuyen doi cao nhat", "nguon nao chuyen doi tot", "kenh nao chat luong nhat"]): return "best_source_conversion"
        if has_any(q2, ["doanh thu cao nhat", "mang lai doanh thu", "mang tien ve nhieu", "kenh nao doanh thu tot", "nguon nao doanh thu cao"]): return "best_source_revenue"
        if has_any(q2, ["roi cao nhat", "loi nhat", "hieu qua dau tu", "dau tu kenh nao tot", "kenh nao dang loi nhat"]): return "best_source_roi"
        if has_any(q2, ["cost per lead", "cpl", "chi phi moi lead", "gia moi lead", "ton bao nhieu tien moi lead"]): return "source_cpl"
        if has_any(q2, ["cost per win", "cpw", "chi phi moi win", "chi phi moi khach hang thang", "ton bao nhieu tien moi won"]): return "source_cpw"

    # PIPELINE COVERAGE
    if has_any(q2, ["pipeline", "pipeline coverage", "open pipeline", "du target", "dat target", "chi tieu", "co du de dat target", "co dat chi tieu"]):
        if has_any(q2, ["cao nhat", "seller nao", "ai cao nhat", "sale nao", "nguoi nao"]): return "pipeline_best_seller"
        if has_any(q2, ["du target", "dat target", "co du", "du de dat", "co dat chi tieu"]): return "pipeline_enough_target"
        return "pipeline_current"

    # LOST ANALYSIS
    if has_any(q2, ["lost", "mat khach", "that bai", "khong chot", "rot lead", "khach tu choi", "khach khong mua", "fail", "that thoat", "ly do rot", "nguyen nhan rot", "vi sao khach rot"]):
        if has_any(q2, ["ly do pho bien", "pho bien nhat", "lost pho bien", "lost pho bien nhat", "ly do lost pho bien", "ly do lost pho bien nhat", "nguyen nhan pho bien", "ly do nhieu nhat", "lost nhieu nhat", "vi sao mat nhieu", "vi sao khach rot", "khach tu choi vi dau", "nguyen nhan chinh", "ly do chinh"]): return "lost_reason_most_common"
        if has_any(q2, ["gia", "price", "gia cao", "mac", "qua dat", "lien quan den gia", "vi gia", "khong du ngan sach"]): return "lost_reason_price"
        if has_any(q2, ["danh sach", "tat ca", "liet ke", "thong ke tung ly do", "so luong tung ly do"]): return "lost_reason_list"
        if has_any(q2, ["top 5", "top nam", "5 ly do"]): return "top_5_lost_reasons"
        if has_any(q2, ["ti le cao nhat", "ty le cao nhat", "chiem ti le cao nhat", "chiem ty le cao nhat"]): return "lost_reason_highest_rate"
        if has_any(q2, ["ti le", "ty le", "lost rate", "phan tram", "rate"]):
            if has_any(q2, ["seller", "sale", "nhan vien", "nguoi ban"]): return "lost_seller_highest_rate"
            return "lost_rate_current"
        if has_any(q2, ["tong", "bao nhieu", "co may", "so luong"]): return "total_lost"
        if has_any(q2, ["seller", "sale", "sales owner", "nhan vien", "ai lam mat khach", "ben nao lam mat khach"]): return "lost_seller_most_count"
        if has_any(q2, ["nguon", "source", "kenh", "kenh nao kem", "lead kem", "chat luong kem"]): return "lost_source_most"
        if has_any(q2, ["region", "khu vuc", "vung", "mien nao", "noi nao"]): return "lost_region_most"
        if has_any(q2, ["industry", "nganh", "nganh nghe", "linh vuc nao"]): return "lost_industry_most"
        return "lead_lost"

    # REVENUE ANALYSIS
    if has_any(q2, ["doanh thu", "revenue", "tong tien", "tong doanh thu", "tien won", "tien ban duoc", "kiem duoc bao nhieu", "kiem tien", "mang tien", "dem tien", "tao doanh thu", "doanh so"]):
        if has_any(q2, ["thang nay", "doanh thu thang nay", "revenue this month"]): return "revenue_this_month"
        if has_any(q2, ["thang truoc", "doanh thu thang truoc", "last month"]): return "revenue_last_month"
        if has_any(q2, ["tang hay giam", "so voi thang truoc", "chenh lech", "bien dong doanh thu", "xu huong doanh thu"]): return "revenue_month_compare"
        if has_any(q2, ["trung binh moi lead won", "tb moi lead won", "avg revenue per won", "trung binh moi deal won"]): return "revenue_avg_per_won"

        # LEAD SOURCE REVENUE
        if has_any(q2, ["lead source", "source", "nguon", "nguon lead", "kenh", "kenh lead", "kenh marketing"]):
            if has_any(q2, ["top 5", "top nam", "5 nguon", "5 kenh"]): return "revenue_source_top5"
            if has_any(q2, ["cao nhat", "tot nhat", "nhieu nhat", "manh nhat", "dan dau", "top dau"]): return "revenue_source_best"
            if has_any(q2, ["thap nhat", "kem nhat", "it nhat", "yeu nhat", "cuoi bang"]): return "revenue_source_lowest"
            if has_any(q2, ["trung binh", "avg", "average"]): return "revenue_source_avg"
            return "revenue_source_list"

        # PRODUCT LINE REVENUE
        if has_any(q2, ["product", "product line", "san pham", "dong san pham", "mat hang", "hang hoa"]):
            if has_any(q2, ["top 5", "top nam", "5 san pham"]): return "revenue_product_top5"
            if has_any(q2, ["cao nhat", "tot nhat", "nhieu nhat", "manh nhat", "ban chay nhat"]): return "revenue_product_best"
            if has_any(q2, ["thap nhat", "kem nhat", "it nhat", "yeu nhat"]): return "revenue_product_lowest"
            if has_any(q2, ["trung binh", "avg", "average"]): return "revenue_product_avg"
            return "revenue_product_list"

        # REGION REVENUE
        if has_any(q2, ["region", "khu vuc", "vung", "mien", "dia ban"]):
            if has_any(q2, ["cao nhat", "tot nhat", "nhieu nhat", "manh nhat", "dan dau"]): return "revenue_region_best"
            if has_any(q2, ["thap nhat", "kem nhat", "it nhat", "yeu nhat"]): return "revenue_region_lowest"
            if has_any(q2, ["trung binh", "avg", "average"]): return "revenue_region_avg"
            return "revenue_region_list"

        # INDUSTRY REVENUE
        if has_any(q2, ["industry", "nganh", "nganh nghe", "linh vuc"]):
            if has_any(q2, ["cao nhat", "tot nhat", "nhieu nhat", "manh nhat"]): return "revenue_industry_best"
            if has_any(q2, ["thap nhat", "kem nhat", "it nhat", "yeu nhat"]): return "revenue_industry_lowest"
            if has_any(q2, ["trung binh", "avg", "average"]): return "revenue_industry_avg"
            return "revenue_industry_list"

        # SELLER REVENUE
        if has_any(q, ["seller", "sale", "sales owner", "nhan vien", "nguoi ban"]):
        # Ưu tiên kiểm tra các điều kiện cụ thể (thấp nhất) trước
            if has_any(q2, ["bottom 5", "5 thap nhat", "5 kem nhat"]): return "revenue_seller_bottom5"
            if has_any(q2, ["top 5", "top nam", "5 seller", "5 sale"]): return "top_5_seller_revenue"
            if has_any(q2, ["cao nhat", "tot nhat", "nhieu nhat"]): return "best_seller_revenue"
            if has_any(q2, ["thap nhat", "kem nhat"]): return "revenue_seller_lowest"
            return "revenue_seller_list"

        if has_any(q2, ["tong doanh thu", "tong tien won", "tong doanh so", "tong tien ban duoc"]): return "revenue_total_won"
        if has_any(q2, ["doanh thu hien tai", "hien tai", "hom nay", "doanh thu bay gio", "revenue current"]): return "revenue_current"
        return "revenue_current"

    # LEAD OVERVIEW
    if has_any(q2, ["tong quan", "tinh hinh", "dashboard", "bao cao nhanh", "hom nay the nao", "lead hien tai sao roi", "tong lead", "co bao nhieu lead", "co may lead"]): return "lead_summary"
    if "trang thai lead" in q2 or "lead theo trang thai" in q2 or "status" in q2: return "lead_status"
    if "new" in q2: return "lead_new"
    if "connected" in q2: return "lead_connected"
    if "qualified" in q2: return "lead_qualified"
    if "won" in q2 and not has_any(q2, ["ti le", "ty le", "rate"]): return "lead_won"
    if "lost" in q2 and not has_any(q2, ["ti le", "ty le", "rate"]): return "lead_lost"
    if "won_rate" in q2 or has_any(q2, ["ti le won", "ty le won", "won rate"]): return "won_rate"
    if "lost_rate" in q2 or has_any(q2, ["ti le lost", "ty le lost", "lost rate"]): return "lost_rate"

    return "unknown"


# =====================================================
# FORECAST & WHAT-IF INTERPRETER ENGINE (DYNAMIC REAL DATA)
# =====================================================

def analyze_forecast_by_intent(intent, question):
    ctx = get_dashboard_context()
    q, q2 = normalize_question(question)
    
    # 1. REVENUE FORECAST GROUP
    if intent in ["forecast_revenue_month", "forecast_revenue_quarter", "forecast_revenue_year", "forecast_revenue_trend", "forecast_revenue_growth"]:
        summary = get_revenue_summary_data()
        this_month = summary.get("thisMonthRevenue", 0)
        last_month = summary.get("lastMonthRevenue", 0)
        current_rev = summary.get("totalRevenue", 0)
        
        growth_rate = ((this_month - last_month) / last_month) if last_month > 0 else 0.05
        pred_month = this_month * (1 + growth_rate) if this_month > 0 else (current_rev / 4)
        pred_quarter = pred_month * 3
        
        # -------------------------------------------------
        # THUẬT TOÁN ĐÀO SÂU: KHỚP SỐ CHU KỲ LỊCH SỬ CHUẨN TỪ SPRING BOOT
        # -------------------------------------------------
        pipelines = get_pipeline_data()
        
        # Ép kiểu so sánh string/int an toàn tuyệt đối với dữ liệu thực tế
        m5_pipeline = sum(float(x.get("openPipeline") or 0) for x in pipelines if str(x.get("periodMonth")) == "5")
        m6_pipeline = sum(float(x.get("openPipeline") or 0) for x in pipelines if str(x.get("periodMonth")) == "6")
        
        win_fraction = (ctx["won_rate"] / 100) if ctx["won_rate"] > 0 else 0.22
        
        # Tạo fallback dữ liệu nếu database của tháng cũ trống
        rev_q1_simulated = m5_pipeline * win_fraction if m5_pipeline > 0 else (this_month * 3 * 0.9)
        rev_q2_simulated = m6_pipeline * win_fraction if m6_pipeline > 0 else (this_month * 3 * 0.95)
        
        avg_historical_month = (m5_pipeline + m6_pipeline) / 2 * win_fraction
        
        # 3. So sánh doanh thu dự báo tháng với doanh thu trung bình tháng quá khứ
        diff_historical = pred_month - avg_historical_month
        growth_lbl = "TĂNG" if diff_historical >= 0 else "GIẢM"
        percent_historical = (abs(diff_historical) / avg_historical_month) * 100 if avg_historical_month > 0 else 0
        if intent == "forecast_revenue_month":
            # Tính toán dựa trên số liệu thực tế
            growth_rate = ((this_month - last_month) / last_month) if last_month > 0 else 0.05
            pred_month = this_month * (1 + growth_rate) if this_month > 0 else (current_rev / 4)
            
            # Tính trung bình doanh thu các tháng gần nhất để so sánh
            # Ở đây ta lấy trung bình của 2 tháng gần nhất (last_month và this_month)
            avg_past_month = (this_month + last_month) / 2
            
            diff = pred_month - avg_past_month
            percent_change = (diff / avg_past_month) * 100 if avg_past_month > 0 else 0
            trend_lbl = "TĂNG" if diff >= 0 else "GIẢM"

            return build_ai_answer(
                title="📈 DỰ BÁO DOANH THU THÁNG TIẾP THEO",
                result=(
                    f"- Doanh thu ước tính tháng sau: {pred_month:,.0f} VNĐ\n"
                    f"- So sánh với trung bình 2 tháng gần nhất: {trend_lbl} {abs(percent_change):.2f}% "
                    f"({trend_lbl} tương đương {abs(diff):,.0f} VNĐ)"
                ),
                insight=(
                    f"Dựa trên dữ liệu thực tế:\n"
                    f"- Doanh thu tháng này: {this_month:,.0f} VNĐ\n"
                    f"- Doanh thu tháng trước: {last_month:,.0f} VNĐ\n"
                    f"Tốc độ tăng trưởng đang duy trì ở mức {growth_rate*100:.2f}%. "
                    f"Dự báo được tính toán bằng cách áp dụng tốc độ này lên doanh thu tháng hiện tại."
                ),
                follow_group="revenue"
            )
            
        if intent == "forecast_revenue_quarter":
            # Tạo chuỗi mô tả động minh bạch nguồn gốc số liệu
            base_reason_text = (
                f"1. Thuật toán quét tổng Open Pipeline hiện có của phòng kinh doanh đạt {sum(float(x['openPipeline']) for x in pipelines):,.0f} VNĐ.\n"
                f"2. Áp dụng tỷ lệ chốt đơn thành công hiện hành (Win Rate chung đạt {ctx['won_rate']}%).\n"
            )
            
            if m5_pipeline > 0 or m6_pipeline > 0:
                base_reason_text += f"3. Hệ thống đối chiếu thành công lịch sử Open Pipeline thực tế ghi nhận của Tháng 5 ({m5_pipeline:,.0f} VNĐ) và Tháng 6 ({m6_pipeline:,.0f} VNĐ) từ hệ thống."
            else:
                base_reason_text += f"3. Do dữ liệu lưu kho Pipeline Tháng 5 và Tháng 6 chưa tích lũy đủ chu kỳ, hệ thống tự động áp dụng mô hình dự báo tăng trưởng dựa trên doanh thu thực tế tháng này ({this_month:,.0f} VNĐ) để tính biên độ an toàn."

            return build_ai_answer(
                title="📊 DỰ BÁO DOANH THU QUÝ TIẾP THEO (CHUẨN HÓA)",
                result=(
                    f"- Doanh thu mục tiêu chốt dự kiến (3 tháng tới): {pred_quarter:,.0f} VNĐ\n"
                    f"- So sánh hiệu suất dự báo theo tháng với trung bình quá khứ: {growth_lbl} {percent_historical:.2f}% "
                    f"({growth_lbl} tương đương {abs(diff_historical):,.0f} VNĐ/tháng)"
                ),
                insight=(
                    f"💡 CƠ SỞ DỰ ĐOÁN:\n"
                    f"Hệ thống đã chuẩn hóa so sánh dựa trên doanh thu trung bình mỗi tháng. "
                    f"Dự báo tháng tới ({pred_month:,.0f} VNĐ) đang {growth_lbl.lower()} {percent_historical:.2f}% so với mức trung bình {avg_historical_month:,.0f} VNĐ/tháng của chu kỳ trước."
                ),
                follow_group="revenue"
            )

        if intent == "forecast_revenue_year":
            pred_year = (current_rev + pred_quarter * 2) * 1.15
            return build_ai_answer(
                title="🗓️ KỲ VỌNG DOANH THU DÀI HẠN NĂM TIẾP THEO (2027)",
                result=f"- Doanh thu dự báo toàn năm tới đạt: {pred_year:,.0f} VNĐ",
                insight="Đề xuất đẩy mạnh phân bổ ngân sách Marketing vào các kênh có ROI cao từ đầu chu kỳ kinh doanh.",
                follow_group="revenue"
            )

    # 2. LEAD / WON / LOST FORECAST
    if intent in ["forecast_lead_month", "forecast_lead_quarter"]:
        pred_leads = ctx["total"] * 1.08 if ctx["total"] > 0 else 100
        return build_ai_answer(
            title="🎯 DỰ BÁO LƯU LƯỢNG LEAD (KHÁCH TIỀM NĂNG)",
            result=f"- Số lượng Lead mới ước tính nạp vào hệ thống: {pred_leads:,.0f} data khách hàng",
            insight="Tương tác tệp khách hàng thô trên các kênh truyền thông số đang có dấu hiệu cải thiện.",
            follow_group="source"
        )
    if intent == "forecast_won_quarter":
        pred_won = ctx["won"] * 1.05 if ctx["won"] > 0 else 30
        return build_ai_answer(
            title="✅ DỰ BÁO SỐ LƯỢNG KHÁCH HÀNG THẮNG (WON DEALS) QUÝ TỚI",
            result=f"- Dự kiến số deal chốt thành công: {pred_won:,.0f} đơn hàng thành công\n- Tỷ lệ chốt ước tính (Win Rate): {ctx['won_rate']}%",
            insight="Tập trung nguồn lực đẩy nhanh tiến độ chốt số ở bộ phận Proposal Sent.",
            follow_group="seller"
        )
    if intent == "forecast_lost_quarter":
        pred_lost = ctx["lost"] * 1.05 if ctx["lost"] > 0 else 50
        return build_ai_answer(
            title="⚠️ CẢNH BÁO SỐ LƯỢNG LEAD THẤT BẠI (LOST FORECAST)",
            result=f"- Số lượng lead rủi ro thất bại dự kiến: {pred_lost:,.0f} khách hàng rớt",
            insight="Lý do về giá và thương thảo hợp đồng vẫn chiếm tỷ trọng lớn nhất trong nhóm Lost Reasons.",
            follow_group="revenue"
        )
    if intent in ["forecast_conversion", "forecast_win_rate", "forecast_lost_rate"]:
        return build_ai_answer(
            title="📉 DỰ BÁO CHỈ SỐ BIẾN ĐỘNG CHUYỂN ĐỔI (RATES FORECAST)",
            result=f"- Win Rate dự kiến: {ctx['won_rate']}%\n- Lost Rate dự kiến: {ctx['lost_rate']}%",
            insight="Các chỉ số này phụ thuộc nhiều vào tốc độ tương tác cuộc gọi đầu tiên (Connected Rate) của nhóm tư vấn.",
            follow_group="seller"
        )

    # 3. SELLER FORECAST GROUP
    if intent.startswith("forecast_seller"):
        sellers = get_sales_owner_data()
        if not sellers: return "Không tìm thấy dữ liệu cấu trúc Seller để dự báo."
        if intent == "forecast_best_seller":
            best = max(sellers, key=lambda x: x["totalRevenue"])
            return build_ai_answer(
                title="🏆 DỰ BÁO SELLER DẪN ĐẦU DOANH SỐ (STAR SELLER)",
                result=f"- Đại diện xuất sắc dự kiến: {best['userName']}\n- Doanh thu kỳ vọng mang về: {best['totalRevenue']:,.0f} VNĐ\n- Win Rate hiện hành: {best['winRate']}%",
                insight="Nhân sự này sở hữu chỉ số ngày chốt deal trung bình ngắn nhất, có kỹ năng tối ưu tệp Open Lead hiệu quả.",
                follow_group="seller"
            )
        if intent == "forecast_top5_seller":
            top5 = sorted(sellers, key=lambda x: x["totalRevenue"], reverse=True)[:5]
            res_text = ""
            for i, s in enumerate(top5, 1):
                res_text += f"{i}. {s['userName']} | Doanh số tích lũy: {s['totalRevenue']:,.0f} VNĐ\n"
            return build_ai_answer(title="📋 TOP 5 SELLER SẼ DẪN ĐẦU DOANH SỐ QUÝ TỚI", result=res_text, insight="Đây là lực lượng nòng cốt gánh phần lớn mục tiêu doanh số của dự án CRM.", follow_group="seller")
        if intent == "forecast_seller_risk":
            risk = max(sellers, key=lambda x: x["openLead"])
            return build_ai_answer(
                title="⚠️ CẢNH BÁO RỦI RO HIỆU SUẤT NHÂN SỰ",
                result=f"- Nhân sự cảnh báo: {risk['userName']}\n- Số lượng Lead Open tồn đọng: {risk['openLead']:,.0f} khách hàng chưa xử lý xong",
                insight="Lượng lead phân bổ quá tải khiến chu kỳ chăm sóc bị ngâm lâu, đẩy cao rủi ro khách hàng huỷ deal.",
                follow_group="seller"
            )

    # 4. KÊNH / VÙNG / NGÀNH / SẢN PHẨM FORECAST
    if "source" in intent:
        data = get_lead_source_data()
        if not data: return "Không lấy được thông tin kênh Marketing."
        if "risk" in intent:
            worst = min([x for x in data if x["revenue"] > 0], key=lambda x: x["roi"], default=data[0])
            return build_ai_answer(title="⚠️ CẢNH BÁO KÊNH MARKETING KÉM HIỆU QUẢ", result=f"- Nguồn rủi ro: {worst['leadSource']}\n- ROI ghi nhận: {worst['roi']}%\n- CPL tiêu hao: {worst['costPerLead']:,.0f} VNĐ/lead", insight="Chi phí vận hành quảng cáo lớn nhưng tỷ lệ chuyển đổi yếu. Khuyến nghị cắt giảm ngân sách.", follow_group="source")
        best = max(data, key=lambda x: x["revenue"])
        return build_ai_answer(title="💎 DỰ BÁO KÊNH ĐEM LẠI DOANH THU CAO NHẤT", result=f"- Kênh tiếp thị chiến lược: {best['leadSource']}\n- Doanh thu mang về: {best['revenue']:,.0f} VNĐ\n- Chỉ số ROI: {best['roi']}%", insight="Đây là nguồn data có nồng độ khách hàng nét cao, đề xuất bổ sung thêm ngân sách phân bổ số.", follow_group="source")

    if "region" in intent:
        data = get_revenue_region_data()
        if not data: return "Không lấy được thông tin khu vực địa lý."
        if "risk" in intent:
            worst = min(data, key=lambda x: x["revenue"])
            return build_ai_answer(title="⚠️ KHU VỰC THỊ TRƯỜNG HOẠT ĐỘNG KÉM", result=f"- Vùng địa lý rủi ro: {worst['name']}\n- Doanh thu: {worst['revenue']:,.0f} VNĐ", insight="Sức mua tại địa bàn này đang chững lại, cần triển khai các chính sách thúc đẩy thương mại cục bộ.", follow_group="region")
        best = max(data, key=lambda x: x["revenue"])
        return build_ai_answer(title="🗺️ DỰ BÁO MIỀN THỊ TRƯỜNG PHÁT TRIỂN MẠNH NHẤT", result=f"- Khu vực trọng điểm: {best['name']}\n- Dòng tiền ghi nhận: {best['revenue']:,.0f} VNĐ", insight="Khu vực này đang có nhu cầu tiêu thụ giải pháp rất mạnh, cần ưu tiên cử các seller có kinh nghiệm khai phá.", follow_group="region")

    if "industry" in intent:
        data = get_revenue_industry_data()
        if not data: return "Không lấy được thông tin nhóm ngành."
        if "risk" in intent:
            worst = min(data, key=lambda x: x["revenue"])
            return build_ai_answer(title="⚠️ PHÂN KHÚC NGÀNH NGHỀ CÓ RỦI RO SUY GIẢM", result=f"- Lĩnh vực suy thoái dự kiến: {worst['name']}\n- Giá trị: {worst['revenue']:,.0f} VNĐ", insight="Khách hàng doanh nghiệp thuộc ngành này đang siết chặt chi phí đầu tư công nghệ, chu kỳ ngâm deal lâu.", follow_group="industry")
        best = max(data, key=lambda x: x["revenue"])
        return build_ai_answer(title="🏢 DỰ BÁO PHÂN KHÚC NGÀNH DOANH THU LỚN NHẤT", result=f"- Ngành kinh tế chủ lực: {best['name']}\n- Quy mô doanh thu chốt: {best['revenue']:,.0f} VNĐ", insight="Nhu cầu chuyển đổi số toàn ngành đang tăng cao, đây là mỏ vàng cần tập trung khai thác.", follow_group="industry")

    if "product" in intent:
        data = get_revenue_product_line_data()
        if not data: return "Không lấy được dữ liệu dòng sản phẩm."
        if "risk" in intent:
            worst = min(data, key=lambda x: x["revenue"])
            return build_ai_answer(title="⚠️ DÒNG SẢN PHẨM CÓ NGUY CƠ DOANH THU THẤP", result=f"- Mặt hàng rủi ro tồn kho/ế: {worst['name']}\n- Doanh số thu về: {worst['revenue']:,.0f} VNĐ", insight="Cần cơ cấu lại tính năng hoặc thay đổi phương án chiết khấu giá để tăng tính cạnh tranh.", follow_group="product")
        best = max(data, key=lambda x: x["revenue"])
        return build_ai_answer(title="📦 DỰ BÁO DÒNG SẢN PHẨM BÁN CHẠY NHẤT (PRODUCT POTENTIAL)", result=f"- Sản phẩm ngôi sao: {best['name']}\n- Doanh thu tạo lập: {best['revenue']:,.0f} VNĐ", insight="Sản phẩm này đang có độ phủ thị trường cực tốt, là mũi nhọn tiếp cận khách hàng mới.", follow_group="product")

    # 5. TARGET / KPI GOAL FORECAST
    if intent.startswith("forecast_target_"):
        pipelines = get_pipeline_data()
        total_pipe = sum(x["openPipeline"] for x in pipelines)
        
        target_kpi = sum(x["target"] for x in pipelines)
        if target_kpi <= 0:
            target_kpi = 45000000000  
        
        win_fraction = (ctx["won_rate"] / 100) if ctx["won_rate"] > 0 else 0.22
        est_closing = total_pipe * win_fraction
        is_safe = est_closing >= target_kpi
        gap_money = target_kpi - est_closing
        
        if intent == "forecast_target_achievable":
            status_lbl = "✅ KHẢ NĂNG CAO HOÀN THÀNH CHỈ TIÊU" if is_safe else "⚠️ NGUY CƠ KHÔNG ĐẠT ĐƯỢC TARGET (RỦI RO)"
            return build_ai_answer(
                title="🎯 THẨM ĐỊNH NĂNG LỰC ĐẠT KPI TARGET DOANH NGHIỆP",
                result=f"- Đánh giá trạng thái: {status_lbl}\n- Doanh thu chốt dự đoán: {est_closing:,.0f} VNĐ\n- Chỉ tiêu Target cam kết: {target_kpi:,.0f} VNĐ",
                insight=f"Tổng Open Pipeline đang giữ là {total_pipe:,.0f} VNĐ. Khả năng phễu phụ thuộc vào năng lực thúc đẩy deal của bộ phận Sales.",
                follow_group="revenue"
            )
        if intent in ["forecast_target_probability", "forecast_target_gap", "forecast_target_revenue_gap"]:
            return build_ai_answer(
                title="📊 PHÂN TÍCH KHOẢNG CÁCH THIẾU HỤT DOANH SỐ (GAP ANALYSIS)",
                result=f"- Khoản tiền thiếu hụt cần bù lấp: {max(0, gap_money):,.0f} VNĐ\n- Tỷ lệ hoàn thành an toàn hiện tại: {(est_closing / target_kpi) * 100:.2f}%",
                insight=f"Hệ thống yêu cầu Sales Admin cần nạp thêm tối thiểu {max(0, gap_money)/0.25:,.0f} VNĐ giá trị cơ hội mới vào Pipeline để bảo hiểm rủi ro.",
                follow_group="revenue"
            )
        if intent == "forecast_target_lead_gap":
            avg_rev_per_win = get_revenue_summary_data().get("avgRevenuePerWonLead", 250000000)
            needed_wins = max(0, gap_money) / avg_rev_per_win if avg_rev_per_win > 0 else 5
            needed_leads = needed_wins / win_fraction if win_fraction > 0 else 40
            return build_ai_answer(
                title="🎯 DỰ TOÁN LƯỢNG LEAD VÀ DEAL CẦN BỔ SUNG ĐỂ ĐẠT TARGET",
                result=f"- Số lượng Lead Marketing cần nạp thêm: {needed_leads:,.0f} khách tiềm năng\n- Số lượng deal Won cần chốt thêm: {needed_wins:.1f} hợp đồng thành công",
                insight=f"Phép toán tính dựa trên giá trị đơn hàng trung bình lịch sử hệ thống ({avg_rev_per_win:,.0f} VNĐ/deal).",
                follow_group="source"
            )

    # 6. WHAT-IF SIMULATION ANALYSIS (Bóc tách số liệu thực tế nhân sự)
    if intent.startswith("what_if_"):
        summary = get_revenue_summary_data()
        current_revenue = summary.get("totalRevenue", 0)
        
        if intent == "what_if_seller":
            sellers = get_sales_owner_data()
            target_seller = None
            
            for s in sellers:
                name_no_accent = remove_accents(s["userName"].lower())
                if name_no_accent in q2:
                    target_seller = s
                    break
                    
            if target_seller:
                s_name = target_seller["userName"]
                s_rev = target_seller["totalRevenue"] 
                s_win_rate = target_seller["winRate"]
                s_open_lead = target_seller["openLead"]
                
                if current_revenue <= 0:
                    current_revenue = sum(x["totalRevenue"] for x in sellers)
                
                drop_percent = round((s_rev / current_revenue) * 100, 2) if current_revenue > 0 else 0
                simulated_total = max(0, current_revenue - s_rev)
                
                return build_ai_answer(
                    title=f"💡 GIẢ LẬP RỦI RO THỰC TẾ: NẾU SELLER {s_name.upper()} KHÔNG ĐẠT TARGET QUÝ 3",
                    result=(
                        f"- Doanh thu thực tế hiện tại của {s_name}: {s_rev:,.0f} VNĐ\n"
                        f"- Tổng doanh thu toàn doanh nghiệp hiện tại: {current_revenue:,.0f} VNĐ\n"
                        f"- Mức độ tác động thiệt hại: -{drop_percent}% doanh số toàn công ty\n"
                        f"- Doanh thu tổng sau sụt giảm giả định: {simulated_total:,.0f} VNĐ"
                    ),
                    insight=(
                        f"Dựa trên số liệu, hệ thống tính toán thấy {s_name} đang gánh vác vị thế lớn với {drop_percent}% tổng doanh thu toàn doanh nghiệp. "
                        f"Nếu nhân sự này hụt chỉ tiêu, cán cân doanh thu tổng sẽ tụt trực tiếp về mốc {simulated_total:,.0f} VNĐ. "
                        f"Sales Admin cần tập trung hỗ trợ tháo gỡ và chốt gấp {s_open_lead:,.0f} cơ hội bán hàng (Open Lead) mà nhân sự này đang phụ trách "
                        f"để đảm bảo duy trì tỷ lệ chiến thắng {s_win_rate}%."
                    ),
                    follow_group="seller"
                )
            else:
                return build_ai_answer(
                    title="💡 KỊCH BẢN GIẢ LẬP: NẾU NHÓM SELLER BOTTOM KHÔNG ĐẠT CHỈ TIÊU",
                    result="- Áp lực Pipeline tồn đọng: Tăng 20% nguy cơ nghẽn phễu.\n- Rủi ro dồn gánh nặng doanh số lên nhóm Top Seller.",
                    insight="Hệ thống khuyến nghị cần thiết lập chính sách kiểm soát sát sao nhóm nhân sự có Win Rate thấp dưới 15% để tránh kéo tụt KPI chung.",
                    follow_group="seller"
                )
                
        if intent == "what_if_win_rate":
            sim_rev = current_revenue * 1.12
            return build_ai_answer(
                title="💡 KỊCH BẢN GIẢ LẬP: NẾU WIN RATE TĂNG THÊM 5%",
                result=f"- Tổng doanh thu mô phỏng mới: {sim_rev:,.0f} VNĐ\n- Giá trị thặng dư tạo thêm cho công ty: +{sim_rev - current_revenue:,.0f} VNĐ",
                insight="Tăng 5% tỷ lệ chốt đơn giúp tối ưu hoá toàn bộ lượng tệp khách hàng cũ mà không làm gia tăng bất kỳ chi phí chạy quảng cáo nào.",
                follow_group="seller"
            )
        if intent == "what_if_conversion":
            sim_rev = current_revenue * 1.08
            return build_ai_answer(
                title="💡 KỊCH BẢN GIẢ LẬP: NẾU TỶ LỆ CHUYỂN ĐỔI CHUNG CẢI THIỆN",
                result=f"- Doanh thu giả định mới: {sim_rev:,.0f} VNĐ\n- Mức tăng trưởng tương ứng: +8% dòng tiền đổ về",
                insight="Đề xuất tổ chức huấn luyện chuyên sâu kỹ năng gọi điện khai thác nhu cầu sơ bộ ở trạng thái Connected.",
                follow_group="revenue"
            )
        if intent in ["what_if_lead", "what_if_budget"]:
            sources = get_lead_source_data()
            if not sources: return "Không lấy được dữ liệu nguồn để chạy mô phỏng."
            best_src = max(sources, key=lambda x: x["revenue"])
            sim_added = best_src["revenue"] * 0.25
            return build_ai_answer(
                title=f"💡 GIẢ LẬP: TĂNG 25% NGÂN SÁCH/DATA VÀO KÊNH MARKETING HIỆU QUẢ NHẤT ({best_src['leadSource']})",
                result=f"- Doanh thu hệ thống tăng thêm ước tính: +{sim_added:,.0f} VNĐ\n- Doanh thu tổng hợp sau giả lập: {current_revenue + sim_added:,.0f} VNĐ",
                insight=f"Bơm tài nguyên vào nguồn {best_src['leadSource']} mang lại hiệu quả an toàn nhất cho doanh nghiệp nhờ chỉ số ROI hiện hành rất cao.",
                follow_group="source"
            )

    return "Không thể nhận diện kịch bản dự báo tương ứng."


# =========================
# MAIN CHATBOT
# =========================

def ask_dashboard(question):
    q, q2 = normalize_question(question)

    intent = detect_intent(question)

    print("QUESTION =", question)
    print("Q2 =", q2)
    print("INTENT =", intent)

    # SALES OWNER
    if intent in [
        "best_seller_revenue",
        "top_5_seller_revenue",
        "revenue_seller_bottom5",
        "best_seller_win_rate",
        "seller_most_open_leads",
        "seller_fastest"
    ]:
        return analyze_sales_owner_by_intent(intent)

    # LEAD SOURCE
    if intent in [
        "best_source_leads",
        "best_source_conversion",
        "best_source_revenue",
        "best_source_roi",
        "source_cpl",
        "source_cpw"
    ]:
        return analyze_lead_source_by_intent(intent)

    # PIPELINE COVERAGE
    if intent in [
        "pipeline_current",
        "pipeline_best_seller",
        "pipeline_enough_target"
    ]:
        return analyze_pipeline_by_intent(intent)

    # LOST ANALYSIS
    if intent in [
        "lost_reason_most_common",
        "lost_reason_price",
        "lost_rate_current",
        "total_lost",
        "lost_reason_list",
        "lost_reason_highest_rate",
        "lost_seller_highest_rate",
        "lost_seller_most_count",
        "lost_source_most",
        "lost_region_most",
        "lost_industry_most",
        "top_5_lost_reasons"
    ]:
        return analyze_lost_by_intent(intent, question)
        
    # REVENUE ANALYSIS
    if intent.startswith("revenue_"):
        return analyze_revenue_by_intent(intent, question)
        
    # REDIRECT FORECAST AND WHAT-IF TO INTERPRETER
    if intent.startswith("forecast_") or intent.startswith("what_if_"):
        return analyze_forecast_by_intent(intent, question)
        
    # LEAD OVERVIEW
    ctx = get_dashboard_context()

    if ctx["total"] == 0 and not ctx["status_data"]:
        return "Tôi chưa lấy được dữ liệu từ API /lead-status."

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

    return (
        "Tôi chưa hiểu rõ câu hỏi.\n"
        "Bạn có thể hỏi tự nhiên như:\n"
        "- Khách rớt nhiều nhất vì lý do gì?\n"
        "- Bên sale nào đang làm mất khách nhiều nhất?\n"
        "- Kênh nào lead kém nhất?\n"
        "- Region nào đang Lost nhiều nhất?\n"
        "- Top 5 lý do Lost phổ biến nhất?"
    )

@app.post("/ask")
def ask(request: QuestionRequest):
    # Extract year (e.g. 2023, 2024, 2025, 2026, 2027)
    year_match = re.search(r'\b(202\d)\b', request.question)
    year = int(year_match.group(1)) if year_match else None
    
    # Extract quarter (e.g. quý 1, q2)
    q_match = re.search(r'(?:quý|q)\s*([1-4])', request.question, re.IGNORECASE)
    quarter = int(q_match.group(1)) if q_match else None

    # Set context variables for the API calls in this request session
    y_token = current_year.set(year)
    q_token = current_quarter.set(quarter)

    try:
        answer = ask_dashboard(request.question)
    finally:
        current_year.reset(y_token)
        current_quarter.reset(q_token)

    return {
        "question": request.question,
        "answer": answer,
        "time": datetime.now().strftime("%d/%m/%Y %H:%M")
    }


@app.get("/health")
def health():
    return {
        "status": "AI service is running"
    }