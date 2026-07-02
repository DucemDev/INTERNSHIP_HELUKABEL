# BÁO CÁO PHÂN TÍCH API & BỘ LỌC DASHBOARD

Báo cáo này chia nhỏ danh sách các API và bộ lọc được định nghĩa trong [DashboardController.java](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/java/com/helu/internship/controller/DashboardController.java) thành các bảng riêng biệt giúp bạn dễ dàng theo dõi phần nào **ĐÃ** và **CHƯA** hiển thị lên giao diện (UI).

---

## I. NHÓM 1: CÁC API ĐÃ HIỂN THỊ TRÊN GIAO DIỆN (32 API)

Đây là những API đã được gọi bằng Ajax/Fetch ở Frontend và hiển thị dữ liệu thành công lên biểu đồ hoặc KPI cards.

| STT | Endpoint (Backend) | Vị Trí Hiển Thị Trên Giao Diện | File Frontend Sử Dụng |
|---|---|---|---|
| 1 | `/api/dashboard/lead-status` | Biểu đồ Phễu 3D & số lượng lead New, Qualified, Contacted | [dashboard-home.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-home.html) |
| 2 | `/api/dashboard/leads-by-status` | Danh sách chi tiết khi click vào tầng của biểu đồ phễu | [dashboard-home.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-home.html) |
| 3 | `/api/dashboard/conversion-rate` | Thẻ KPI: Tỷ lệ chuyển đổi chung | [dashboard-home.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-home.html) |
| 4 | `/api/dashboard/average-days-to-won` | Thẻ KPI: Vòng đời chốt deal (Avg Sales Cycle) | [dashboard-home.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-home.html) |
| 5 | `/api/dashboard/revenue-summary` | Thẻ KPI: Doanh thu Won | [dashboard-home.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-home.html) |
| 6 | `/api/dashboard/pipeline-coverage` | Thẻ KPI: Giá trị Pipeline | [dashboard-home.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-home.html) |
| 7 | `/api/dashboard/lead-source-cost` | Thẻ KPI: Tổng chi phí & CPL. Biểu đồ: Phân bổ chi phí | [dashboard-home.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-home.html) |
| 8 | `/api/dashboard/best-industry-won-deal` | Thẻ KPI: Ngành có nhiều Won Deal nhất | [dashboard-home.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-home.html) |
| 9 | `/api/dashboard/best-region-won-deal` | Thẻ KPI: Vùng có nhiều Won Deal nhất | [dashboard-home.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-home.html) |
| 10 | `/api/dashboard/daily-compare` | Chỉ số % tăng trưởng hàng ngày ở các KPI card | [dashboard-home.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-home.html) |
| 11 | `/api/dashboard/cost-per-win-source` | Biểu đồ: Hiệu quả nguồn lead (Bar: Avg Cost/Won) | [dashboard-home.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-home.html) |
| 12 | `/api/dashboard/roi-lead-source` | Biểu đồ: ROI theo nguồn lead | [dashboard-home.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-home.html) |
| 13 | `/api/dashboard/win-rate-by-region` | Biểu đồ: Ma trận bong bóng phân tích chéo | [dashboard-home.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-home.html) |
| 14 | `/api/dashboard/win-rate-by-industry` | Biểu đồ: Ma trận bong bóng phân tích chéo | [dashboard-home.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-home.html) |
| 15 | `/api/dashboard/conversion-rate/filter` | Cập nhật chỉ số khi click lọc chéo trên ma trận bong bóng | [dashboard-home.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-home.html) |
| 16 | `/api/dashboard/sales-owner-dashboard-by-quarter`| Bảng xếp hạng: Hiệu quả kinh doanh của seller theo quý | [dashboard-home.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-home.html) |
| 17 | `/api/dashboard/win-rate-by-saleowner-by-quarter`| Bảng xếp hạng: Tỷ lệ thắng của seller theo quý | [dashboard-home.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-home.html) |
| 18 | `/api/dashboard/pipeline-coverage-by-quarter`| Bảng xếp hạng: Độ bao phủ pipeline của seller theo quý | [dashboard-home.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-home.html) |
| 19 | `/api/dashboard/customer-value-matrix` | Biểu đồ phân tán: Ma trận giá trị khách hàng | [customer-value-matrix.js](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/static/js/customer-value-matrix.js) |
| 20 | `/api/dashboard/revenue-industry` | Biểu đồ phân tích doanh thu theo ngành | [customer-analysis.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/customer-analysis.html) |
| 21 | `/api/dashboard/revenue-region` | Biểu đồ phân tích doanh thu theo vùng | [customer-analysis.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/customer-analysis.html) |
| 22 | `/api/dashboard/best-account-revenue` | Top khách hàng có doanh thu lớn nhất | [customer-analysis.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/customer-analysis.html) |
| 23 | `/api/dashboard/best-industry-revenue` | Ngành hàng đem lại doanh thu lớn nhất | [customer-analysis.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/customer-analysis.html) |
| 24 | `/api/dashboard/best-region-revenue` | Vùng đem lại doanh thu lớn nhất | [customer-analysis.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/customer-analysis.html) |
| 25 | `/api/dashboard/best-customer-group-lead`| Nhóm khách hàng đem lại nhiều lead nhất | [customer-analysis.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/customer-analysis.html) |
| 26 | `/api/dashboard/best-customer-group-revenue`| Nhóm khách hàng đem lại nhiều doanh thu nhất | [customer-analysis.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/customer-analysis.html) |
| 27 | `/api/dashboard/seller/stats` | KPI tổng hợp của Seller (Lead, Won, Lost, Win-rate) | [dashboard-seller.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-seller.html) |
| 28 | `/api/dashboard/seller/pipeline-coverage`| Biểu đồ Pipeline Coverage dành cho Seller | [dashboard-seller.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-seller.html) |
| 29 | `/api/dashboard/seller/leads-by-status-count`| Thống kê trạng thái Lead cho Seller | [dashboard-seller.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-seller.html) |
| 30 | `/api/dashboard/lost-reasons` | Biểu đồ phân tích các lý do mất deal (thất bại) | [dashboard-test.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-test.html) |
| 31 | `/api/dashboard/lost-by-seller` | Phân tích mất deal theo từng nhân viên kinh doanh | [dashboard-test.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-test.html) |
| 32 | `/api/dashboard/lost-by-source` / `/lost-by-region` / `/lost-by-industry` | Thống kê số lượng mất deal theo nguồn, vùng, ngành | [dashboard-test.html](file:///D:/thuctap/INTERNSHIP_HELUKABEL/INTERNSHIP_HELUKABEL/src/main/resources/templates/dashboard/dashboard-test.html) |

---

## II. NHÓM 2: CÁC API CHƯA HIỂN THỊ TRÊN GIAO DIỆN CHÍNH (55 API)

Đây là những API đã được viết ở Backend nhưng **chưa được gắn vào giao diện dashboard chính** để sử dụng.

| STT | Endpoint (Backend) | Lý Do Chưa Xuất Hiện Trên UI / Hướng Phát Triển |
|---|---|---|
| 1 | `/api/dashboard/win-rate-by-saleowner` | Tỷ lệ thắng theo nhân viên (Chưa vẽ biểu đồ so sánh) |
| 2 | `/api/dashboard/sales-owner-dashboard` | Tổng quan số liệu kinh doanh của tất cả seller (không lọc theo quý) |
| 3 | `/api/dashboard/revenue/account` | Thống kê doanh thu chi tiết từng công ty/tài khoản khách hàng |
| 4 | `/api/dashboard/potential-lead/industry`| Phân tích chi tiết lead tiềm năng theo ngành |
| 5 | `/api/dashboard/lead-funnel/source` | Phễu khách hàng chi tiết cho từng Nguồn lead |
| 6 | `/api/dashboard/lead-source-by-product`| Ma trận phân bổ: Dòng sản phẩm đi kèm với Nguồn lead |
| 7 | `/api/dashboard/revenue-by-source-product`| Báo cáo doanh thu theo sự kết hợp giữa Nguồn và Sản phẩm |
| 8 | `/api/dashboard/won-lead-by-source-product`| Số deal thành công theo nguồn và dòng sản phẩm |
| 9 | `/api/dashboard/lost-lead-by-source-product`| Số deal thất bại theo nguồn và dòng sản phẩm |
| 10 | `/api/dashboard/top10-accounts` | Bảng xếp hạng Top 10 khách hàng VIP (Backend đã có nhưng UI chưa hiển thị bảng) |
| 11 | `/api/dashboard/top-sales-owner-revenue` | Thẻ vinh danh/Biểu đồ: Seller có doanh số cao nhất |
| 12 | `/api/dashboard/top-sales-owner-win-rate`| Thẻ vinh danh/Biểu đồ: Seller có tỉ lệ chốt deal tốt nhất |
| 13 | `/api/dashboard/fastest-sales-owner` | Thẻ vinh danh: Seller chốt deal nhanh nhất |
| 14 | `/api/dashboard/sales-owner/avg-sales-cycle`| Biểu đồ: So sánh chu kỳ bán hàng trung bình giữa các seller |
| 15 | `/api/dashboard/sales-owner/bant-complete-rate`| Biểu đồ cột: % điểm kiểm soát khách hàng tiềm năng (BANT) của seller |
| 16 | `/api/dashboard/sales-owner/avg-bant-score` | Biểu đồ cột: Điểm chất lượng lead BANT của seller |
| 17 | `/api/dashboard/product-line/loss-reasons`| Thống kê lý do thất bại cụ thể cho từng dòng sản phẩm |
| 18 | `/api/dashboard/sales-owner/detail` | Trang Dashboard con: Xem báo cáo hiệu suất chi tiết của 1 seller cụ thể |
| 19-33 | Các API `/api/dashboard/industry/...`<br> (total-leads, won-leads, conversion-rate, avg-sales-cycle, best-lost-reason) | Nhóm API phân tích sâu về Ngành nghề (Chưa có trang dashboard chi tiết riêng cho cấu trúc Ngành) |
| 34-48 | Các API `/api/dashboard/customer-role/...`<br>(revenue, total-leads, conversion-rate, avg-revenue-won, lost-leads, best-lost-reason) | Nhóm API phân tích sâu theo Chức danh/Vai trò của người liên hệ bên phía khách hàng |
| 49-55 | Các API `/api/dashboard/region/...` và `/api/dashboard/product-line/...` phân rã | Nhóm API phân rã sâu về Vùng địa lý và Dòng sản phẩm (Backend đã hỗ trợ chi tiết nhưng UI chỉ vẽ tổng quát) |

---

## III. NHÓM 3: CÁC API BỊ LỖI 404 (GỌI SAI ENDPOINT Ở FRONTEND)

Đây là những endpoint **Frontend gọi nhưng Backend không có**, dẫn đến lỗi tải trang (404 Not Found) trong bảng điều khiển.

| Endpoint Frontend Đang Gọi | File Frontend Thực Hiện | Nguyên Nhân & Cách Khắc Phục |
|---|---|---|
| `/api/dashboard/revenue-role` | `customer-analysis.html:659` | **Sai tên**: Backend chỉ có `/api/dashboard/customer-role/revenue`. Cần đổi lại endpoint gọi ở frontend. |
| `/api/dashboard/revenue-group` | `customer-analysis.html:661` | **Thiếu API**: Backend chưa định nghĩa API thống kê doanh thu theo nhóm khách hàng. |
| `/api/dashboard/revenue-product` | `customer-analysis.html:662` | **Thiếu API**: Backend chưa có API này. Cần đổi sang `/api/dashboard/revenue-product-line` (đã có ở Backend). |
| `/api/dashboard/total-accounts` | `customer-analysis.html:972` | **Thiếu API**: Backend chưa hỗ trợ đếm tổng số lượng tài khoản (Accounts). |
| `/api/dashboard/won-accounts` | `customer-analysis.html:980` | **Thiếu API**: Backend chưa hỗ trợ đếm số lượng tài khoản đã win. |
| `/api/dashboard/top-underserved-segment`| `customer-analysis.html:988` | **Thiếu API**: Backend chưa hỗ trợ tính toán phân khúc khách hàng chưa khai thác tốt. |

---

## IV. BẢNG PHÂN TÍCH BỘ LỌC (FILTERS)

| Bộ Lọc (Filter Parameter) | Trạng Thái Trên UI | Hoạt Động Thực Tế |
|---|---|---|
| **Lọc theo Năm (Year)** | **ĐÃ HIỂN THỊ** | Có `<select id="yearFilter">` ở trên cùng bên phải. Tác động trực tiếp lên API so sánh doanh số theo quý. |
| **Lọc theo Vùng (Region) & Ngành (Industry)** | **ĐÃ HIỂN THỊ (LỌC CHÉO)** | Người dùng click vào bong bóng trên biểu đồ ma trận bong bóng để lọc nhanh tỷ lệ chuyển đổi. |
| **Lọc khoảng thời gian (timeFrom -> timeTo)** | **CHƯA HIỂN THỊ** | Backend có tham số lọc ngày trong API `/conversion-rate/filter` nhưng UI chưa có ô chọn ngày (Date Picker). |
| **Lọc chi tiết nguồn lead (sourceId, sourceType)** | **CHƯA HIỂN THỊ** | Backend hỗ trợ lọc tỷ lệ chuyển đổi và phễu theo nguồn cụ thể, UI chưa có dropdown chọn nguồn. |
| **Lọc theo Seller (salesOwnerId)** | **CHƯA HIỂN THỊ** | Backend hỗ trợ lọc tỷ lệ chuyển đổi theo nhân viên, UI chưa có dropdown chọn nhân viên để lọc. |
| **Lọc theo sản phẩm thất bại (productId)** | **CHƯA HIỂN THỊ** | API lý do mất deal (`/lost-reasons`) có nhận `productId` để xem chi tiết lý do mất deal của sản phẩm đó, nhưng UI chưa có dropdown chọn sản phẩm để lọc. |



    # Cài đặt nếu chưa có                                                                                                                        
    pip install fastapi uvicorn[standard] requests pydantic                                                                                      
                                                                                                                                                 
    # Khởi động server                                                                                                                           
    uvicorn app:app --host 0.0.0.0 --port 8000 --reload    