# 🤖 Helukabel CRM Chatbot — Phiên bản Cải tiến (Production-Ready)

Thư mục này chứa phiên bản cải tiến của chatbot CRM. Toàn bộ mã nguồn đã được tái cấu trúc từ file đơn lẻ `app.py` sang kiến trúc modular sạch sẽ, bảo mật và tối ưu hiệu năng.

---

## 📁 Cấu trúc thư mục

* **`main.py`**: FastAPI application entry point, chứa CORS, Auth Middleware, và API routes.
* **`config.py`**: Lưu trữ các hằng số cấu hình (URL backend, CORS origins, Cache TTL, ...).
* **`api_client.py`**: Async HTTP client (sử dụng `httpx`) tích hợp bộ nhớ đệm TTL cache 5 phút để tránh gọi API lặp lại.
* **`nlp_utils.py`**: Tiện ích xử lý ngôn ngữ tự nhiên tiếng Việt, sửa lỗi false positive bằng kiểm tra ranh giới từ (word boundary).
* **`intent_detector.py`**: Hệ thống nhận diện ý định của người dùng dựa trên từ khóa tối ưu.
* **`analyzers.py`**: Toàn bộ logic phân tích dữ liệu từ Spring Boot backend và định dạng câu trả lời tiếng Việt.
* **`requirements.txt`**: Khai báo thư viện phụ thuộc.

---

## 🚀 Tính năng cải tiến chính

1. **Bảo mật (CORS & Auth)**:
   * Chỉ cho phép frontend Spring Boot kết nối (`http://localhost:8080`).
   * Tích hợp Auth Middleware kiểm tra JWT/Token trong header `Authorization` hoặc `JSESSIONID` cookie của Spring Boot.
2. **Hiệu năng (Async & Caching)**:
   * Đổi từ `requests` (đồng bộ/blocking) sang `httpx` (bất đồng bộ/async).
   * TTL Cache giúp giảm tải cho Java backend (chỉ query DB tối đa 1 lần mỗi 5 phút cho cùng một metric).
3. **Độ chính xác (Fix False Positive)**:
   * Sử dụng Regular Expressions để kiểm tra ranh giới từ (VD: từ "new" trong câu "renewable energy" sẽ không bị nhận nhầm thành trạng thái "New").
4. **Minh bạch Forecast**:
   * Thêm cảnh báo (disclaimer) cho các kết quả dự báo và giả lập kịch bản.

---

## 💻 Hướng dẫn chạy và kiểm tra

### 1. Cài đặt thư viện
Cài đặt các thư viện phụ thuộc từ file `requirements.txt`:
* **Nếu đứng ở thư mục gốc dự án**:
  ```bash
  pip install -r chatbot/requirements.txt
  ```
* **Nếu đứng ở thư mục `chatbot`**:
  ```bash
  pip install -r requirements.txt
  ```

### 2. Khởi chạy Chatbot Service
Sử dụng `uvicorn` để chạy server trên cổng `8000`. Nhờ cấu hình dynamic path, bạn có thể chạy bằng một trong hai cách sau:

* **Cách 1: Chạy từ thư mục gốc dự án (Khuyên dùng)**:
  ```bash
  uvicorn chatbot.main:app --reload --port 8000
  ```
* **Cách 2: Chạy từ bên trong thư mục `chatbot`**:
  ```bash
  cd chatbot
  uvicorn main:app --reload --port 8000
  ```

### 3. Kiểm tra Health Check
Mở trình duyệt truy cập: [http://localhost:8000/health](http://localhost:8000/health) để xem trạng thái dịch vụ.

### 4. Gọi API hỏi đáp (Cần gửi Auth header hoặc Cookie)
Endpoint hỏi đáp: `POST http://localhost:8000/ask`
* **Headers**:
  * `Content-Type: application/json`
  * `Authorization: Bearer <your_jwt_token>` (hoặc có cookie `JSESSIONID` trong request)
* **Body**:
  ```json
  {
    "question": "Seller nào doanh thu cao nhất?"
  }
  ```
