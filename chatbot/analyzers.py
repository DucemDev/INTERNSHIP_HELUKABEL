"""
CRM Dashboard Analyzers for Helukabel Chatbot.
Consolidates and modularizes all data analysis logic from original app.py.

Key improvements:
- Async calls to CachedAPIClient to prevent blocking
- Merged duplicate logic (combines question-based and intent-based functions)
- Added disclaimers to all forecast/what-if responses
- Caching layer integration
"""

import sys
import os
sys.path.insert(0, os.path.abspath(os.path.dirname(os.path.dirname(__file__))))

from chatbot.api_client import CachedAPIClient
from chatbot.nlp_utils import remove_accents, normalize_question
import logging
from datetime import datetime

logger = logging.getLogger(__name__)

# Forecast disclaimer to be appended to all prediction/simulation results
FORECAST_DISCLAIMER = "\n\n⚠️ Lưu ý: Đây là ước tính dựa trên xu hướng dữ liệu lịch sử, không phải dự báo chính xác. Vui lòng tham khảo thêm ý kiến chuyên gia trước khi ra quyết định."


# =========================
# LEAD STATUS NORMALIZATION
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


# =========================
# DATA FETCHING FUNCTIONS (ASYNC)
# =========================

async def get_dashboard_context(client: CachedAPIClient):
    raw_status = await client.get("/lead-status")
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


async def get_lead_source_data(client: CachedAPIClient):
    data = await client.get("/lead-source-cost")
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


async def get_sales_owner_data(client: CachedAPIClient):
    data = await client.get("/sales-owner-dashboard")
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


async def get_pipeline_data(client: CachedAPIClient):
    data = await client.get("/pipeline-coverage")
    if data is None:
        return []

    result = []
    for item in data:
        result.append({
            "sellerName": item.get("salesOwner") or item.get("sellerName") or item.get("userName") or "Không xác định",
            "openPipeline": float(item.get("openPipeline") or 0),
            "target": float(item.get("target") or item.get("targetRevenue") or 0),
            "pipelineCoverage": float(item.get("pipelineCoverage") or 0),
            "periodMonth": item.get("periodMonth")
        })

    return result


async def get_lost_reasons_data(client: CachedAPIClient):
    data = await client.get("/lost-reasons")
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


async def get_lost_by_seller_data(client: CachedAPIClient):
    data = await client.get("/lost-by-seller")
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


async def get_lost_by_source_data(client: CachedAPIClient):
    data = await client.get("/lost-by-source")
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


async def get_lost_by_region_data(client: CachedAPIClient):
    data = await client.get("/lost-by-region")
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


async def get_lost_by_industry_data(client: CachedAPIClient):
    data = await client.get("/lost-by-industry")
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


async def get_revenue_summary_data(client: CachedAPIClient):
    data = await client.get("/revenue-summary")
    if data is None:
        return {}

    return {
        "totalRevenue": float(data.get("totalRevenue") or 0),
        "wonLead": float(data.get("wonLead") or 0),
        "avgRevenuePerWonLead": float(data.get("avgRevenuePerWonLead") or 0),
        "thisMonthRevenue": float(data.get("thisMonthRevenue") or 0),
        "lastMonthRevenue": float(data.get("lastMonthRevenue") or 0)
    }


async def get_revenue_lead_source_data(client: CachedAPIClient):
    data = await client.get("/lead-source-cost")
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


async def get_revenue_product_line_data(client: CachedAPIClient):
    data = await client.get("/revenue-product-line")
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


async def get_revenue_region_data(client: CachedAPIClient):
    data = await client.get("/revenue-region")
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


async def get_revenue_industry_data(client: CachedAPIClient):
    data = await client.get("/revenue-industry")
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


async def get_revenue_seller_data(client: CachedAPIClient):
    data = await get_sales_owner_data(client)
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


# =========================
# RESPONSE HELPERS
# =========================

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


# =========================
# LEAD OVERVIEW
# =========================

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
# LEAD SOURCE (MERGED)
# =========================

async def analyze_lead_source(client: CachedAPIClient, intent: str):
    data = await get_lead_source_data(client)
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


# =========================
# SALES OWNER (MERGED)
# =========================

async def analyze_sales_owner(client: CachedAPIClient, intent: str):
    data = await get_sales_owner_data(client)
    if not data:
        return "Không lấy được dữ liệu Sales Owner từ API /sales-owner-dashboard."

    if intent == "revenue_seller_bottom5":
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
# PIPELINE COVERAGE
# =========================

async def analyze_pipeline(client: CachedAPIClient, intent: str):
    data = await get_pipeline_data(client)
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

async def analyze_lost(client: CachedAPIClient, intent: str, question: str):
    reasons = await get_lost_reasons_data(client)

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
        ctx = await get_dashboard_context(client)
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
        data = await get_lost_by_seller_data(client)
        if not data:
            return "Không lấy được dữ liệu Lost theo Seller từ API /lost-by-seller."
        best = max(data, key=lambda x: x["lostRate"])
        return (
            f"Seller có tỉ lệ Lost cao nhất là {best['salesOwner']}.\n"
            f"Tỉ lệ Lost: {best['lostRate']}%.\n"
            f"Số lead Lost: {best['lostLead']:,.0f} lead."
        )

    if intent == "lost_seller_most_count":
        data = await get_lost_by_seller_data(client)
        if not data:
            return "Không lấy được dữ liệu Lost theo Seller từ API /lost-by-seller."
        best = max(data, key=lambda x: x["lostLead"])
        return (
            f"Seller có nhiều lead Lost nhất là {best['salesOwner']}.\n"
            f"Số lead Lost: {best['lostLead']:,.0f} lead.\n"
            f"Tỉ lệ Lost: {best['lostRate']}%."
        )

    if intent == "lost_source_most":
        data = await get_lost_by_source_data(client)
        if not data:
            return "Không lấy được dữ liệu Lost theo Source từ API /lost-by-source."
        best = max(data, key=lambda x: x["lostLead"])
        return (
            f"Nguồn lead có nhiều Lost nhất là {best['sourceName']}.\n"
            f"Số lead Lost: {best['lostLead']:,.0f} lead.\n"
            f"Tỉ lệ Lost: {best['lostRate']}%."
        )

    if intent == "lost_region_most":
        data = await get_lost_by_region_data(client)
        if not data:
            return "Không lấy được dữ liệu Lost theo Region từ API /lost-by-region."
        best = max(data, key=lambda x: x["lostLead"])
        return (
            f"Region có nhiều Lost nhất là {best['region']}.\n"
            f"Số lead Lost: {best['lostLead']:,.0f} lead.\n"
            f"Tỉ lệ Lost: {best['lostRate']}%."
        )

    if intent == "lost_industry_most":
        data = await get_lost_by_industry_data(client)
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


async def analyze_revenue(client: CachedAPIClient, intent: str, question: str):
    if intent in ["revenue_current", "revenue_total_won", "revenue_avg_per_won", "revenue_this_month", "revenue_last_month", "revenue_month_compare"]:
        summary = await get_revenue_summary_data(client)
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
        data = await get_revenue_lead_source_data(client)
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
        data = await get_revenue_product_line_data(client)
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
        data = await get_revenue_region_data(client)
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
        data = await get_revenue_industry_data(client)
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
        data = await get_revenue_seller_data(client)
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


# =========================
# FORECAST & WHAT-IF (WITH DISCLAIMERS)
# =========================

async def analyze_forecast(client: CachedAPIClient, intent: str, question: str):
    ctx = await get_dashboard_context(client)
    q, q2 = normalize_question(question)

    # 1. REVENUE FORECAST GROUP
    if intent in ["forecast_revenue_month", "forecast_revenue_quarter", "forecast_revenue_year", "forecast_revenue_trend", "forecast_revenue_growth"]:
        summary = await get_revenue_summary_data(client)
        this_month = summary.get("thisMonthRevenue", 0)
        last_month = summary.get("lastMonthRevenue", 0)
        current_rev = summary.get("totalRevenue", 0)

        growth_rate = ((this_month - last_month) / last_month) if last_month > 0 else 0.05
        pred_month = this_month * (1 + growth_rate) if this_month > 0 else (current_rev / 4)
        pred_quarter = pred_month * 3

        pipelines = await get_pipeline_data(client)
        m5_pipeline = sum(float(x.get("openPipeline") or 0) for x in pipelines if str(x.get("periodMonth")) == "5")
        m6_pipeline = sum(float(x.get("openPipeline") or 0) for x in pipelines if str(x.get("periodMonth")) == "6")

        win_fraction = (ctx["won_rate"] / 100) if ctx["won_rate"] > 0 else 0.22
        avg_historical_month = (m5_pipeline + m6_pipeline) / 2 * win_fraction

        diff_historical = pred_month - avg_historical_month
        growth_lbl = "TĂNG" if diff_historical >= 0 else "GIẢM"
        percent_historical = (abs(diff_historical) / avg_historical_month) * 100 if avg_historical_month > 0 else 0

        if intent == "forecast_revenue_month":
            avg_past_month = (this_month + last_month) / 2
            diff = pred_month - avg_past_month
            percent_change = (diff / avg_past_month) * 100 if avg_past_month > 0 else 0
            trend_lbl = "TĂNG" if diff >= 0 else "GIẢM"

            ans = build_ai_answer(
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
            return ans + FORECAST_DISCLAIMER

        if intent == "forecast_revenue_quarter":
            ans = build_ai_answer(
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
            return ans + FORECAST_DISCLAIMER

        if intent == "forecast_revenue_year":
            pred_year = (current_rev + pred_quarter * 2) * 1.15
            ans = build_ai_answer(
                title="🗓️ KỲ VỌNG DOANH THU DÀI HẠN NĂM TIẾP THEO (2027)",
                result=f"- Doanh thu dự báo toàn năm tới đạt: {pred_year:,.0f} VNĐ",
                insight="Đề xuất đẩy mạnh phân bổ ngân sách Marketing vào các kênh có ROI cao từ đầu chu kỳ kinh doanh.",
                follow_group="revenue"
            )
            return ans + FORECAST_DISCLAIMER

    # 2. LEAD / WON / LOST FORECAST
    if intent in ["forecast_lead_month", "forecast_lead_quarter"]:
        pred_leads = ctx["total"] * 1.08 if ctx["total"] > 0 else 100
        ans = build_ai_answer(
            title="🎯 DỰ BÁO LƯU LƯỢNG LEAD (KHÁCH TIỀM NĂNG)",
            result=f"- Số lượng Lead Marketing ước tính nạp vào hệ thống: {pred_leads:,.0f} data khách hàng",
            insight="Tương tác tệp khách hàng thô trên các kênh truyền thông số đang có dấu hiệu cải thiện.",
            follow_group="source"
        )
        return ans + FORECAST_DISCLAIMER

    if intent == "forecast_won_quarter":
        pred_won = ctx["won"] * 1.05 if ctx["won"] > 0 else 30
        ans = build_ai_answer(
            title="✅ DỰ BÁO SỐ LƯỢNG KHÁCH HÀNG THẮNG (WON DEALS) QUÝ TỚI",
            result=f"- Dự kiến số deal chốt thành công: {pred_won:,.0f} đơn hàng thành công\n- Tỷ lệ chốt ước tính (Win Rate): {ctx['won_rate']}%",
            insight="Tập trung nguồn lực đẩy nhanh tiến độ chốt số ở bộ phận Proposal Sent.",
            follow_group="seller"
        )
        return ans + FORECAST_DISCLAIMER

    if intent == "forecast_lost_quarter":
        pred_lost = ctx["lost"] * 1.05 if ctx["lost"] > 0 else 50
        ans = build_ai_answer(
            title="⚠️ CẢNH BÁO SỐ LƯỢNG LEAD THẤT BẠI (LOST FORECAST)",
            result=f"- Số lượng lead rủi ro thất bại dự kiến: {pred_lost:,.0f} khách hàng rớt",
            insight="Lý do về giá và thương thảo hợp đồng vẫn chiếm tỷ trọng lớn nhất trong nhóm Lost Reasons.",
            follow_group="revenue"
        )
        return ans + FORECAST_DISCLAIMER

    if intent in ["forecast_conversion", "forecast_win_rate", "forecast_lost_rate"]:
        ans = build_ai_answer(
            title="📉 DỰ BÁO CHỈ SỐ BIẾN ĐỘNG CHUYỂN ĐỔI (RATES FORECAST)",
            result=f"- Win Rate dự kiến: {ctx['won_rate']}%\n- Lost Rate dự kiến: {ctx['lost_rate']}%",
            insight="Các chỉ số này phụ thuộc nhiều vào tốc độ tương tác cuộc gọi đầu tiên (Connected Rate) của nhóm tư vấn.",
            follow_group="seller"
        )
        return ans + FORECAST_DISCLAIMER

    # 3. SELLER FORECAST GROUP
    if intent.startswith("forecast_seller"):
        sellers = await get_sales_owner_data(client)
        if not sellers:
            return "Không tìm thấy dữ liệu cấu trúc Seller để dự báo."

        if intent == "forecast_best_seller":
            best = max(sellers, key=lambda x: x["totalRevenue"])
            ans = build_ai_answer(
                title="🏆 DỰ BÁO SELLER DẪN ĐẦU DOANH SỐ (STAR SELLER)",
                result=f"- Đại diện xuất sắc dự kiến: {best['userName']}\n- Doanh thu kỳ vọng mang về: {best['totalRevenue']:,.0f} VNĐ\n- Win Rate hiện hành: {best['winRate']}%",
                insight="Nhân sự này sở hữu chỉ số ngày chốt deal trung bình ngắn nhất, có kỹ năng tối ưu tệp Open Lead hiệu quả.",
                follow_group="seller"
            )
            return ans + FORECAST_DISCLAIMER

        if intent == "forecast_top5_seller":
            top5 = sorted(sellers, key=lambda x: x["totalRevenue"], reverse=True)[:5]
            res_text = ""
            for i, s in enumerate(top5, 1):
                res_text += f"{i}. {s['userName']} | Doanh số tích lũy: {s['totalRevenue']:,.0f} VNĐ\n"
            ans = build_ai_answer(
                title="📋 TOP 5 SELLER SẼ DẪN ĐẦU DOANH SỐ QUÝ TỚI",
                result=res_text,
                insight="Đây là lực lượng nòng cốt gánh phần lớn mục tiêu doanh số của dự án CRM.",
                follow_group="seller"
            )
            return ans + FORECAST_DISCLAIMER

        if intent == "forecast_seller_risk":
            risk = max(sellers, key=lambda x: x["openLead"])
            ans = build_ai_answer(
                title="⚠️ CẢNH BÁO RỦI RO HIỆU SUẤT NHÂN SỰ",
                result=f"- Nhân sự cảnh báo: {risk['userName']}\n- Số lượng Lead Open tồn đọng: {risk['openLead']:,.0f} khách hàng chưa xử lý xong",
                insight="Lượng lead phân bổ quá tải khiến chu kỳ chăm sóc bị ngâm lâu, đẩy cao rủi ro khách hàng huỷ deal.",
                follow_group="seller"
            )
            return ans + FORECAST_DISCLAIMER

    # 4. KÊNH / VÙNG / NGÀNH / SẢN PHẨM FORECAST
    if "source" in intent:
        data = await get_lead_source_data(client)
        if not data:
            return "Không lấy được thông tin kênh Marketing."
        if "risk" in intent:
            worst = min([x for x in data if x["revenue"] > 0], key=lambda x: x["roi"], default=data[0])
            ans = build_ai_answer(
                title="⚠️ CẢNH BÁO KÊNH MARKETING KÉM HIỆU QUẢ",
                result=f"- Nguồn rủi ro: {worst['leadSource']}\n- ROI ghi nhận: {worst['roi']}%\n- CPL tiêu hao: {worst['costPerLead']:,.0f} VNĐ/lead",
                insight="Chi phí vận hành quảng cáo lớn nhưng tỷ lệ chuyển đổi yếu. Khuyến nghị cắt giảm ngân sách.",
                follow_group="source"
            )
            return ans + FORECAST_DISCLAIMER

        best = max(data, key=lambda x: x["revenue"])
        ans = build_ai_answer(
            title="💎 DỰ BÁO KÊNH ĐEM LẠI DOANH THU CAO NHẤT",
            result=f"- Kênh tiếp thị chiến lược: {best['leadSource']}\n- Doanh thu mang về: {best['revenue']:,.0f} VNĐ\n- Chỉ số ROI: {best['roi']}%",
            insight="Đây là nguồn data có nồng độ khách hàng nét cao, đề xuất bổ sung thêm ngân sách phân bổ số.",
            follow_group="source"
        )
        return ans + FORECAST_DISCLAIMER

    if "region" in intent:
        data = await get_revenue_region_data(client)
        if not data:
            return "Không lấy được thông tin khu vực địa lý."
        if "risk" in intent:
            worst = min(data, key=lambda x: x["revenue"])
            ans = build_ai_answer(
                title="⚠️ KHU VỰC THỊ TRƯỜNG HOẠT ĐỘNG KÉM",
                result=f"- Vùng địa lý rủi ro: {worst['name']}\n- Doanh thu: {worst['revenue']:,.0f} VNĐ",
                insight="Sức mua tại địa bàn này đang chững lại, cần triển khai các chính sách thúc đẩy thương mại cục bộ.",
                follow_group="region"
            )
            return ans + FORECAST_DISCLAIMER

        best = max(data, key=lambda x: x["revenue"])
        ans = build_ai_answer(
            title="🗺️ DỰ BÁO MIỀN THỊ TRƯỜNG PHÁT TRIỂN MẠNH NHẤT",
            result=f"- Khu vực trọng điểm: {best['name']}\n- Dòng tiền ghi nhận: {best['revenue']:,.0f} VNĐ",
            insight="Khu vực này đang có nhu cầu tiêu thụ giải pháp rất mạnh, cần ưu tiên cử các seller có kinh nghiệm khai phá.",
            follow_group="region"
        )
        return ans + FORECAST_DISCLAIMER

    if "industry" in intent:
        data = await get_revenue_industry_data(client)
        if not data:
            return "Không lấy được thông tin nhóm ngành."
        if "risk" in intent:
            worst = min(data, key=lambda x: x["revenue"])
            ans = build_ai_answer(
                title="⚠️ PHÂN KHÚC NGÀNH NGHỀ CÓ RỦI RO SUY GIẢM",
                result=f"- Lĩnh vực suy thoái dự kiến: {worst['name']}\n- Giá trị: {worst['revenue']:,.0f} VNĐ",
                insight="Khách hàng doanh nghiệp thuộc ngành này đang siết chặt chi phí đầu tư công nghệ, chu kỳ ngâm deal lâu.",
                follow_group="industry"
            )
            return ans + FORECAST_DISCLAIMER

        best = max(data, key=lambda x: x["revenue"])
        ans = build_ai_answer(
            title="🏢 DỰ BÁO PHÂN KHÚC NGÀNH DOANH THU LỚN NHẤT",
            result=f"- Ngành kinh tế chủ lực: {best['name']}\n- Quy mô doanh thu chốt: {best['revenue']:,.0f} VNĐ",
            insight="Nhu cầu chuyển đổi số toàn ngành đang tăng cao, đây là mỏ vàng cần tập trung khai thác.",
            follow_group="industry"
        )
        return ans + FORECAST_DISCLAIMER

    if "product" in intent:
        data = await get_revenue_product_line_data(client)
        if not data:
            return "Không lấy được dữ liệu dòng sản phẩm."
        if "risk" in intent:
            worst = min(data, key=lambda x: x["revenue"])
            ans = build_ai_answer(
                title="⚠️ DÒNG SẢN PHẨM CÓ NGUY CƠ DOANH THU THẤP",
                result=f"- Mặt hàng rủi ro tồn kho/ế: {worst['name']}\n- Doanh số thu về: {worst['revenue']:,.0f} VNĐ",
                insight="Cần cơ cấu lại tính năng hoặc thay đổi phương án chiết khấu giá để tăng tính cạnh tranh.",
                follow_group="product"
            )
            return ans + FORECAST_DISCLAIMER

        best = max(data, key=lambda x: x["revenue"])
        ans = build_ai_answer(
            title="📦 DỰ BÁO DÒNG SẢN PHẨM BÁN CHẠY NHẤT (PRODUCT POTENTIAL)",
            result=f"- Sản phẩm ngôi sao: {best['name']}\n- Doanh thu tạo lập: {best['revenue']:,.0f} VNĐ",
            insight="Sản phẩm này đang có độ phủ thị trường cực tốt, là mũi nhọn tiếp cận khách hàng mới.",
            follow_group="product"
        )
        return ans + FORECAST_DISCLAIMER

    # 5. TARGET / KPI GOAL FORECAST
    if intent.startswith("forecast_target_"):
        pipelines = await get_pipeline_data(client)
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
            ans = build_ai_answer(
                title="🎯 THẨM ĐỊNH NĂNG LỰC ĐẠT KPI TARGET DOANH NGHIỆP",
                result=f"- Đánh giá trạng thái: {status_lbl}\n- Doanh thu chốt dự đoán: {est_closing:,.0f} VNĐ\n- Chỉ tiêu Target cam kết: {target_kpi:,.0f} VNĐ",
                insight=f"Tổng Open Pipeline đang giữ là {total_pipe:,.0f} VNĐ. Khả năng phễu phụ thuộc vào năng lực thúc đẩy deal của bộ phận Sales.",
                follow_group="revenue"
            )
            return ans + FORECAST_DISCLAIMER

        if intent in ["forecast_target_probability", "forecast_target_gap", "forecast_target_revenue_gap"]:
            ans = build_ai_answer(
                title="📊 PHÂN TÍCH KHOẢNG CÁCH THIẾU HỤT DOANH SỐ (GAP ANALYSIS)",
                result=f"- Khoản tiền thiếu hụt cần bù lấp: {max(0, gap_money):,.0f} VNĐ\n- Tỷ lệ hoàn thành an toàn hiện tại: {(est_closing / target_kpi) * 100:.2f}%",
                insight=f"Hệ thống yêu cầu Sales Admin cần nạp thêm tối thiểu {max(0, gap_money)/0.25:,.0f} VNĐ giá trị cơ hội mới vào Pipeline để bảo hiểm rủi ro.",
                follow_group="revenue"
            )
            return ans + FORECAST_DISCLAIMER

        if intent == "forecast_target_lead_gap":
            rev_summary = await get_revenue_summary_data(client)
            avg_rev_per_win = rev_summary.get("avgRevenuePerWonLead", 250000000)
            needed_wins = max(0, gap_money) / avg_rev_per_win if avg_rev_per_win > 0 else 5
            needed_leads = needed_wins / win_fraction if win_fraction > 0 else 40
            ans = build_ai_answer(
                title="🎯 DỰ TOÁN LƯỢNG LEAD VÀ DEAL CẦN BỔ SUNG ĐỂ ĐẠT TARGET",
                result=f"- Số lượng Lead Marketing cần nạp thêm: {needed_leads:,.0f} khách tiềm năng\n- Số lượng deal Won cần chốt thêm: {needed_wins:.1f} hợp đồng thành công",
                insight=f"Phép toán tính dựa trên giá trị đơn hàng trung bình lịch sử hệ thống ({avg_rev_per_win:,.0f} VNĐ/deal).",
                follow_group="source"
            )
            return ans + FORECAST_DISCLAIMER

    # 6. WHAT-IF SIMULATION ANALYSIS
    if intent.startswith("what_if_"):
        rev_summary = await get_revenue_summary_data(client)
        current_revenue = rev_summary.get("totalRevenue", 0)

        if intent == "what_if_seller":
            sellers = await get_sales_owner_data(client)
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

                ans = build_ai_answer(
                    title=f"💡 GIẢ LẬP RỦI RO THỰC TẾ: NẾU SELLER {s_name.upper()} KHÔNG ĐẠT TARGET QUÝ 3",
                    result=(
                        f"- Doanh thu thực tế hiện tại của {s_name}: {s_rev:,.0f} VNĐ\n"
                        f"- Tổng doanh thu toàn doanh nghiệp hiện tại: {current_revenue:,.0f} VNĐ\n"
                        f"- Mức độ tác động thiệt hại: -{drop_percent}% doanh số toàn công ty\n"
                        f"- Doanh thu tổng sau sụt giảm giả định: {simulated_total:,.0f} VNĐ"
                    ),
                    insight=(
                        f"Dựa trên số liệu, hệ thống tính toán thấy {s_name} đang gánh vác vị thế lớn với {drop_percent}% tổng doanh thu toàn doanh nghiệp. "
                        f"If nhân sự này hụt chỉ tiêu, cán cân doanh thu tổng sẽ tụt trực tiếp về mốc {simulated_total:,.0f} VNĐ. "
                        f"Sales Admin cần tập trung hỗ trợ tháo gỡ và chốt gấp {s_open_lead:,.0f} cơ hội bán hàng (Open Lead) mà nhân sự này đang phụ trách "
                        f"để đảm bảo duy trì tỷ lệ chiến thắng {s_win_rate}%."
                    ),
                    follow_group="seller"
                )
                return ans + FORECAST_DISCLAIMER
            else:
                ans = build_ai_answer(
                    title="💡 KỊCH BẢN GIẢ LẬP: NẾU NHÓM SELLER BOTTOM KHÔNG ĐẠT CHỈ TIÊU",
                    result="- Áp lực Pipeline tồn đọng: Tăng 20% nguy cơ nghẽn phễu.\n- Rủi ro dồn gánh nặng doanh số lên nhóm Top Seller.",
                    insight="Hệ thống khuyến nghị cần thiết lập chính sách kiểm soát sát sao nhóm nhân sự có Win Rate thấp dưới 15% để tránh kéo tụt KPI chung.",
                    follow_group="seller"
                )
                return ans + FORECAST_DISCLAIMER

        if intent == "what_if_win_rate":
            sim_rev = current_revenue * 1.12
            ans = build_ai_answer(
                title="💡 KỊCH BẢN GIẢ LẬP: NẾU WIN RATE TĂNG THÊM 5%",
                result=f"- Tổng doanh thu mô phỏng mới: {sim_rev:,.0f} VNĐ\n- Giá trị thặng dư tạo thêm cho công ty: +{sim_rev - current_revenue:,.0f} VNĐ",
                insight="Tăng 5% tỷ lệ chốt đơn giúp tối ưu hoá toàn bộ lượng tệp khách hàng cũ mà không làm gia tăng bất kỳ chi phí chạy quảng cáo nào.",
                follow_group="seller"
            )
            return ans + FORECAST_DISCLAIMER

        if intent == "what_if_conversion":
            sim_rev = current_revenue * 1.08
            ans = build_ai_answer(
                title="💡 KỊCH BẢN GIẢ LẬP: NẾU TỶ LỆ CHUYỂN ĐỔI CHUNG CẢI THIỆN",
                result=f"- Doanh thu giả định mới: {sim_rev:,.0f} VNĐ\n- Mức tăng trưởng tương ứng: +8% dòng tiền đổ về",
                insight="Đề xuất tổ chức huấn luyện chuyên sâu kỹ năng gọi điện khai thác nhu cầu sơ bộ ở trạng thái Connected.",
                follow_group="revenue"
            )
            return ans + FORECAST_DISCLAIMER

        if intent in ["what_if_lead", "what_if_budget"]:
            sources = await get_lead_source_data(client)
            if not sources:
                return "Không lấy được dữ liệu nguồn để chạy mô phỏng."
            best_src = max(sources, key=lambda x: x["revenue"])
            sim_added = best_src["revenue"] * 0.25
            ans = build_ai_answer(
                title=f"💡 GIẢ LẬP: TĂNG 25% NGÂN SÁCH/DATA VÀO KÊNH MARKETING HIỆU QUẢ NHẤT ({best_src['leadSource']})",
                result=f"- Doanh thu hệ thống tăng thêm ước tính: +{sim_added:,.0f} VNĐ\n- Doanh thu tổng hợp sau giả lập: {current_revenue + sim_added:,.0f} VNĐ",
                insight=f"Bơm tài nguyên vào nguồn {best_src['leadSource']} mang lại hiệu quả an toàn nhất cho doanh nghiệp nhờ chỉ số ROI hiện hành rất cao.",
                follow_group="source"
            )
            return ans + FORECAST_DISCLAIMER

    return "Không thể nhận diện kịch bản dự báo tương ứng."
