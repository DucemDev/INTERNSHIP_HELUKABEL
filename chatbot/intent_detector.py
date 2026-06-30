"""
Intent Detector for Helukabel CRM Chatbot.
Parses natural language questions and maps them to predefined query intents.

Key improvements:
- Replaced has_any() with has_any_word() to prevent substring false positives
- Fixed false positives for short keywords (new, connected, won, lost, qualified)
  by requiring lead-related context words.
"""

import sys
import os
sys.path.insert(0, os.path.abspath(os.path.dirname(os.path.dirname(__file__))))

from chatbot.nlp_utils import normalize_question, has_any_word, remove_accents

def detect_intent(question: str) -> str:
    q, q2 = normalize_question(question)

    # =====================================================
    # PRIORITY 1: WHAT IF ANALYSIS & FORECAST
    # (Checked first to avoid overlap with current statistics keywords)
    # =====================================================
    if has_any_word(q2, ["neu", "gia su", "what if"]):
        if has_any_word(q2, ["win rate"]): return "what_if_win_rate"
        if has_any_word(q2, ["conversion"]): return "what_if_conversion"
        if has_any_word(q2, ["lead"]): return "what_if_lead"
        if has_any_word(q2, ["ngan sach", "budget"]): return "what_if_budget"
        if has_any_word(q2, ["seller", "sale", "sales owner", "nhan vien", "doanh so", "doanh thu"]): 
            return "what_if_seller"

    FORECAST_WORDS = [
        "du doan", "forecast", "uoc tinh", "du kien",
        "sap toi", "tuong lai", "thang toi", "thang sau",
        "quy toi", "quy sau", "nam toi", "nam sau"
    ]

    if has_any_word(q2, FORECAST_WORDS):
        # REVENUE FORECAST
        if has_any_word(q2, ["doanh thu", "revenue", "doanh so", "tien mang ve", "tien kiem duoc", "sales"]):
            if has_any_word(q2, ["thang sau", "thang toi", "next month"]): return "forecast_revenue_month"
            if has_any_word(q2, ["quy sau", "quy toi", "next quarter"]): return "forecast_revenue_quarter"
            if has_any_word(q2, ["nam sau", "nam toi", "2027", "next year"]): return "forecast_revenue_year"
            if has_any_word(q2, ["xu huong", "tinh hinh", "tang hay giam", "di len", "di xuong", "gan day", "trend"]): return "forecast_revenue_trend"
            if has_any_word(q2, ["tang truong", "growth", "bao nhieu phan tram", "%"]): return "forecast_revenue_growth"

        # LEAD FORECAST
        if has_any_word(q2, ["lead", "khach tiem nang", "khach moi"]):
            if has_any_word(q2, ["thang sau", "thang toi"]): return "forecast_lead_month"
            if has_any_word(q2, ["quy sau", "quy toi"]): return "forecast_lead_quarter"

        # WON FORECAST
        if has_any_word(q2, ["won", "deal thanh cong", "chot thanh cong", "khach mua"]):
            if has_any_word(q2, ["quy sau", "quy toi"]): return "forecast_won_quarter"

        # LOST FORECAST
        if has_any_word(q2, ["lost", "mat khach", "rot lead", "that bai"]):
            if has_any_word(q2, ["quy sau", "quy toi"]): return "forecast_lost_quarter"

        # RATES FORECAST
        if has_any_word(q2, ["conversion", "conversion rate", "ti le chuyen doi", "ty le chuyen doi"]): return "forecast_conversion"
        if has_any_word(q2, ["win rate", "ti le won", "ty le won", "ti le chot"]): return "forecast_win_rate"
        if has_any_word(q2, ["lost rate", "ti le lost", "ty le lost"]): return "forecast_lost_rate"

        # SELLER FORECAST
        if has_any_word(q2, ["seller", "sale", "sales owner", "nhan vien"]):
            if has_any_word(q2, ["top 5", "top seller", "xep hang"]): return "forecast_top5_seller"
            if has_any_word(q2, ["cao nhat", "tot nhat", "dan dau", "tiem nang nhat", "mang ve nhieu tien nhat"]): return "forecast_best_seller"
            if has_any_word(q2, ["giam doanh thu", "rui ro", "di xuong", "can ho tro"]): return "forecast_seller_risk"

        # SOURCE FORECAST
        if has_any_word(q2, ["source", "lead source", "nguon", "kenh"]):
            if has_any_word(q2, ["cao nhat", "tot nhat", "hieu qua nhat", "mang lai nhieu tien nhat"]): return "forecast_best_source"
            if has_any_word(q2, ["giam", "suy giam", "kem hieu qua", "cat ngan sach"]): return "forecast_source_risk"

        # REGION FORECAST
        if has_any_word(q2, ["region", "khu vuc", "vung", "mien"]):
            if has_any_word(q2, ["cao nhat", "tiem nang nhat", "tang truong nhat"]): return "forecast_best_region"
            if has_any_word(q2, ["giam", "suy giam", "rui ro"]): return "forecast_region_risk"

        # INDUSTRY FORECAST
        if has_any_word(q2, ["industry", "nganh", "linh vuc"]):
            if has_any_word(q2, ["cao nhat", "tot nhat", "tiem nang nhat"]): return "forecast_best_industry"
            if has_any_word(q2, ["giam", "suy giam", "rui ro"]): return "forecast_industry_risk"

        # PRODUCT FORECAST
        if has_any_word(q2, ["product", "product line", "san pham", "dong san pham"]):
            if has_any_word(q2, ["cao nhat", "tot nhat", "tiem nang nhat"]): return "forecast_best_product"
            if has_any_word(q2, ["giam", "suy giam", "can xem lai"]): return "forecast_product_risk"

        # TARGET / KPI
        if has_any_word(q2, ["target", "chi tieu", "kpi", "ke hoach"]):
            if has_any_word(q2, ["co dat", "hoan thanh", "dat duoc"]): return "forecast_target_achievable"
            if has_any_word(q2, ["xac suat", "bao nhieu phan tram"]): return "forecast_target_probability"
            if has_any_word(q2, ["con thieu", "gap"]): return "forecast_target_gap"
            if has_any_word(q2, ["can them doanh thu", "them bao nhieu tien"]): return "forecast_target_revenue_gap"
            if has_any_word(q2, ["can them lead", "them bao nhieu lead"]): return "forecast_target_lead_gap"

    # =====================================================
    # PRIORITY 2: CURRENT STATISTICS
    # =====================================================

    # SALES OWNER
    seller_words = ["seller", "sale", "sales owner", "nhân viên", "nhan vien", "người bán", "nguoi ban", "sale nào", "sale nao", "ai bán", "ai ban", "ai chốt", "ai chot"]
    if has_any_word(q, seller_words) or has_any_word(q2, seller_words):
        if has_any_word(q2, ["top 5 doanh thu", "top seller", "xep hang seller", "danh sach seller doanh thu", "5 sale doanh thu"]): return "top_5_seller_revenue"
        if has_any_word(q2, ["doanh thu cao nhat", "ban tot nhat", "kiem tien nhieu nhat", "ai mang tien ve nhieu nhat", "ai doanh thu tot nhat", "sale nao tot nhat", "seller nao tot nhat", "nguoi nao ban duoc nhieu nhat"]): return "best_seller_revenue"
        if has_any_word(q2, ["win rate cao nhat", "ti le thang cao nhat", "ty le thang cao nhat", "chuyen doi tot nhat", "chot tot nhat", "ti le chot cao nhat", "ai chot thanh cong nhieu nhat"]): return "best_seller_win_rate"
        if has_any_word(q2, ["lead open nhieu nhat", "nhieu lead open", "dang co nhieu lead", "dang xu ly nhieu lead", "nhieu lead chua chot", "ai dang om nhieu lead", "seller nao ban ron nhat"]): return "seller_most_open_leads"
        if has_any_word(q2, ["xu ly nhanh nhat", "chot nhanh nhat", "it ngay nhat", "ai xu ly nhanh", "sale nao nhanh nhat", "thoi gian won nhanh nhat", "chot deal nhanh nhat"]): return "seller_fastest"

    # LEAD SOURCE
    source_words = ["nguồn", "nguon", "lead source", "source", "kênh", "kenh", "nguồn lead", "nguon lead", "kênh lead", "kenh lead"]
    if has_any_word(q, source_words) or has_any_word(q2, source_words):
        if has_any_word(q2, ["nhieu lead nhat", "mang lai nhieu khach", "nhieu khach hang nhat", "kenh nao do lead nhieu", "nguon nao co nhieu lead", "nguon nao mang ve nhieu lead"]): return "best_source_leads"
        if has_any_word(q2, ["conversion cao nhat", "conversion rate cao nhat", "ti le chuyen doi cao nhat", "ty le chuyen doi cao nhat", "nguon nao chuyen doi tot", "kenh nao chat luong nhat"]): return "best_source_conversion"
        if has_any_word(q2, ["doanh thu cao nhat", "mang lai doanh thu", "mang tien ve nhieu", "kenh nao doanh thu tot", "nguon nao doanh thu cao"]): return "best_source_revenue"
        if has_any_word(q2, ["roi cao nhat", "loi nhat", "hieu qua dau tu", "dau tu kenh nao tot", "kenh nao dang loi nhat"]): return "best_source_roi"
        if has_any_word(q2, ["cost per lead", "cpl", "chi phi moi lead", "gia moi lead", "ton bao nhieu tien moi lead"]): return "source_cpl"
        if has_any_word(q2, ["cost per win", "cpw", "chi phi moi win", "chi phi moi khach hang thang", "ton bao nhieu tien moi won"]): return "source_cpw"

    # PIPELINE COVERAGE
    if has_any_word(q2, ["pipeline", "pipeline coverage", "open pipeline", "du target", "dat target", "chi tieu", "co du de dat target", "co dat chi tieu"]):
        if has_any_word(q2, ["cao nhat", "seller nao", "ai cao nhat", "sale nao", "nguoi nao"]): return "pipeline_best_seller"
        if has_any_word(q2, ["du target", "dat target", "co du", "du de dat", "co dat chi tieu"]): return "pipeline_enough_target"
        return "pipeline_current"

    # LOST ANALYSIS
    if has_any_word(q2, ["lost", "mat khach", "that bai", "khong chot", "rot lead", "khach tu choi", "khach khong mua", "fail", "that thoat", "ly do rot", "nguyen nhan rot", "vi sao khach rot"]):
        if has_any_word(q2, ["ly do pho bien", "pho bien nhat", "lost pho bien", "lost pho bien nhat", "ly do lost pho bien", "ly do lost pho bien nhat", "nguyen nhan pho bien", "ly do nhieu nhat", "lost nhieu nhat", "vi sao mat nhieu", "vi sao khach rot", "khach tu choi vi dau", "nguyen nhan chinh", "ly do chinh"]): return "lost_reason_most_common"
        if has_any_word(q2, ["gia", "price", "gia cao", "mac", "qua dat", "lien quan den gia", "vi gia", "khong du ngan sach"]): return "lost_reason_price"
        if has_any_word(q2, ["danh sach", "tat ca", "liet ke", "thong ke tung ly do", "so luong tung ly do"]): return "lost_reason_list"
        if has_any_word(q2, ["top 5", "top nam", "5 ly do"]): return "top_5_lost_reasons"
        if has_any_word(q2, ["ti le cao nhat", "ty le cao nhat", "chiem ti le cao nhat", "chiem ty le cao nhat"]): return "lost_reason_highest_rate"
        if has_any_word(q2, ["ti le", "ty le", "lost rate", "phan tram", "rate"]):
            if has_any_word(q2, ["seller", "sale", "nhan vien", "nguoi ban"]): return "lost_seller_highest_rate"
            return "lost_rate_current"
        if has_any_word(q2, ["tong", "bao nhieu", "co may", "so luong"]): return "total_lost"
        if has_any_word(q2, ["seller", "sale", "sales owner", "nhan vien", "ai lam mat khach", "ben nao lam mat khach"]): return "lost_seller_most_count"
        if has_any_word(q2, ["nguon", "source", "kenh", "kenh nao kem", "lead kem", "chat luong kem"]): return "lost_source_most"
        if has_any_word(q2, ["region", "khu vuc", "vung", "mien nao", "noi nao"]): return "lost_region_most"
        if has_any_word(q2, ["industry", "nganh", "nganh nghe", "linh vuc nao"]): return "lost_industry_most"
        return "lead_lost"

    # REVENUE ANALYSIS
    if has_any_word(q2, ["doanh thu", "revenue", "tong tien", "tong doanh thu", "tien won", "tien ban duoc", "kiem duoc bao nhieu", "kiem tien", "mang tien", "dem tien", "tao doanh thu", "doanh so"]):
        if has_any_word(q2, ["thang nay", "doanh thu thang nay", "revenue this month"]): return "revenue_this_month"
        if has_any_word(q2, ["thang truoc", "doanh thu thang truoc", "last month"]): return "revenue_last_month"
        if has_any_word(q2, ["tang hay giam", "so voi thang truoc", "chenh lech", "bien dong doanh thu", "xu huong doanh thu"]): return "revenue_month_compare"
        if has_any_word(q2, ["trung binh moi lead won", "tb moi lead won", "avg revenue per won", "trung binh moi deal won"]): return "revenue_avg_per_won"

        # LEAD SOURCE REVENUE
        if has_any_word(q2, ["lead source", "source", "nguon", "nguon lead", "kenh", "kenh lead", "kenh marketing"]):
            if has_any_word(q2, ["top 5", "top nam", "5 nguon", "5 kenh"]): return "revenue_source_top5"
            if has_any_word(q2, ["cao nhat", "tot nhat", "nhieu nhat", "manh nhat", "dan dau", "top dau"]): return "revenue_source_best"
            if has_any_word(q2, ["thap nhat", "kem nhat", "it nhat", "yeu nhat", "cuoi bang"]): return "revenue_source_lowest"
            if has_any_word(q2, ["trung binh", "avg", "average"]): return "revenue_source_avg"
            return "revenue_source_list"

        # PRODUCT LINE REVENUE
        if has_any_word(q2, ["product", "product line", "san pham", "dong san pham", "mat hang", "hang hoa"]):
            if has_any_word(q2, ["top 5", "top nam", "5 san pham"]): return "revenue_product_top5"
            if has_any_word(q2, ["cao nhat", "tot nhat", "nhieu nhat", "manh nhat", "ban chay nhat"]): return "revenue_product_best"
            if has_any_word(q2, ["thap nhat", "kem nhat", "it nhat", "yeu nhat"]): return "revenue_product_lowest"
            if has_any_word(q2, ["trung binh", "avg", "average"]): return "revenue_product_avg"
            return "revenue_product_list"

        # REGION REVENUE
        if has_any_word(q2, ["region", "khu vuc", "vung", "mien", "dia ban"]):
            if has_any_word(q2, ["cao nhat", "tot nhat", "nhieu nhat", "manh nhat", "dan dau"]): return "revenue_region_best"
            if has_any_word(q2, ["thap nhat", "kem nhat", "it nhat", "yeu nhat"]): return "revenue_region_lowest"
            if has_any_word(q2, ["trung binh", "avg", "average"]): return "revenue_region_avg"
            return "revenue_region_list"

        # INDUSTRY REVENUE
        if has_any_word(q2, ["industry", "nganh", "nganh nghe", "linh vuc"]):
            if has_any_word(q2, ["cao nhat", "tot nhat", "nhieu nhat", "manh nhat"]): return "revenue_industry_best"
            if has_any_word(q2, ["thap nhat", "kem nhat", "it nhat", "yeu nhat"]): return "revenue_industry_lowest"
            if has_any_word(q2, ["trung binh", "avg", "average"]): return "revenue_industry_avg"
            return "revenue_industry_list"

        # SELLER REVENUE
        if has_any_word(q, ["seller", "sale", "sales owner", "nhan vien", "nguoi ban"]):
            if has_any_word(q2, ["bottom 5", "5 thap nhat", "5 kem nhat"]): return "revenue_seller_bottom5"
            if has_any_word(q2, ["top 5", "top nam", "5 seller", "5 sale"]): return "top_5_seller_revenue"
            if has_any_word(q2, ["cao nhat", "tot nhat", "nhieu nhat"]): return "best_seller_revenue"
            if has_any_word(q2, ["thap nhat", "kem nhat"]): return "revenue_seller_lowest"
            return "revenue_seller_list"

        if has_any_word(q2, ["tong doanh thu", "tong tien won", "tong doanh so", "tong tien ban duoc"]): return "revenue_total_won"
        if has_any_word(q2, ["doanh thu hien tai", "hien tai", "hom nay", "doanh thu bay gio", "revenue current"]): return "revenue_current"
        return "revenue_current"

    # LEAD OVERVIEW
    if has_any_word(q2, ["tong quan", "tinh hinh", "dashboard", "bao cao nhanh", "hom nay the nao", "lead hien tai sao roi", "tong lead", "co bao nhieu lead", "co may lead"]): return "lead_summary"
    if "trang thai lead" in q2 or "lead theo trang thai" in q2 or "status" in q2: return "lead_status"

    # Lead status queries - require context to avoid false positives on simple words
    if has_any_word(q2, ["lead new", "trang thai new", "status new", "bao nhieu new", "so luong new", "may lead new"]): return "lead_new"
    if has_any_word(q2, ["lead connected", "trang thai connected", "status connected", "bao nhieu connected"]): return "lead_connected"
    if has_any_word(q2, ["lead qualified", "trang thai qualified", "status qualified", "bao nhieu qualified"]): return "lead_qualified"
    if has_any_word(q2, ["lead won", "trang thai won", "status won", "bao nhieu won", "so lead won", "may lead won"]) and not has_any_word(q2, ["ti le", "ty le", "rate", "win rate"]): return "lead_won"
    if has_any_word(q2, ["lead lost", "trang thai lost", "status lost", "bao nhieu lost", "so lead lost", "may lead lost"]) and not has_any_word(q2, ["ti le", "ty le", "rate", "lost rate"]): return "lead_lost"
    
    if "won_rate" in q2 or has_any_word(q2, ["ti le won", "ty le won", "won rate"]): return "won_rate"
    if "lost_rate" in q2 or has_any_word(q2, ["ti le lost", "ty le lost", "lost rate"]): return "lost_rate"

    return "unknown"
