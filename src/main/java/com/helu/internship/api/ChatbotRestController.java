package com.helu.internship.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotRestController {

    @PostMapping
    public ResponseEntity<Map<String, String>> handleChat(@RequestBody Map<String, String> payload, Principal principal) {
        String userEmail = principal != null ? principal.getName() : "User";
        String message = payload.getOrDefault("message", "").trim();
        String responseText = "";

        String msgLower = message.toLowerCase();
        if (msgLower.isEmpty()) {
            responseText = "Vui lòng nhập câu hỏi của bạn.";
        } else if (msgLower.contains("hello") || msgLower.contains("hi") || msgLower.contains("chào") || msgLower.contains("xin chào")) {
            responseText = "Xin chào! Tôi là **Trợ lý AI HELUKABEL**. Tôi ở đây để hỗ trợ bạn khai thác dữ liệu khách hàng, phân tích tiêu chí BANT, và tối ưu hóa hiệu suất bán hàng. Hôm nay bạn cần tôi trợ giúp gì?";
        } else if (msgLower.contains("bant")) {
            responseText = "Hệ thống HELUKABEL CRM chấm điểm **BANT** theo 4 tiêu chí:\n" +
                    "- 💰 **Budget (Ngân sách - 25đ)**: Khả năng chi trả của khách hàng.\n" +
                    "- 👑 **Authority (Quyền quyết định - 25đ)**: Người liên hệ có quyền ký hợp đồng hay không.\n" +
                    "- 🎯 **Need (Nhu cầu thực tế - 25đ)**: Sự phù hợp của giải pháp cáp Helukabel.\n" +
                    "- ⏳ **Timeline (Thời gian mua - 25đ)**: Thời gian dự kiến lắp đặt/mua hàng.\n\n" +
                    "**Phân loại nhiệt độ Lead:**\n" +
                    "- 🔥 **HOT**: >= 80 điểm.\n" +
                    "- ☀️ **WARM**: 60 - 79 điểm.\n" +
                    "- ❄️ **COLD**: < 60 điểm.";
        } else if (msgLower.contains("doanh thu") || msgLower.contains("revenue") || msgLower.contains("tiền") || msgLower.contains("won")) {
            responseText = "Để xem thống kê doanh thu chính xác theo thời gian thực:\n" +
                    "1. Truy cập mục **Dashboard** hoặc **Sales Performance Analysis**.\n" +
                    "2. Xem biểu đồ doanh thu theo **Dòng sản phẩm**, **Nguồn Lead**, và **Khu vực**.\n" +
                    "3. Doanh thu của bạn được cập nhật tự động ngay khi trạng thái Lead chuyển sang **Won**.";
        } else if (msgLower.contains("lost") || msgLower.contains("mất") || msgLower.contains("thất bại")) {
            responseText = "Các lý do mất lead thường gặp bao gồm: *Giá cao*, *Đối thủ cạnh tranh*, hoặc *Sai quy cách kỹ thuật*. Bạn có thể xem biểu đồ **Lost Reason Summary** tại Dashboard để nắm bắt cụ thể lý do mất khách hàng theo từng dòng sản phẩm.";
        } else if (msgLower.contains("lead") || msgLower.contains("khách hàng")) {
            responseText = "Bạn có thể quản lý danh sách khách hàng tại tab **Quản lý Lead**. Tại đây, bạn có thể lọc lead theo trạng thái, xem phân loại nhiệt độ (Hot/Warm/Cold), đánh giá lại điểm BANT trực tiếp trên bảng hoặc click trực tiếp vào tên khách hàng để xem lịch sử chăm sóc.";
        } else {
            responseText = "Tôi đã ghi nhận câu hỏi về: *\"" + message + "\"*.\n\nHệ thống AI của tôi đang được huấn luyện sâu thêm với cơ sở dữ liệu kỹ thuật của cáp **HELUKABEL**. Hiện tại, tôi khuyên bạn nên sử dụng thanh tìm kiếm hoặc các bộ lọc trực quan ở trang Dashboard và Quản lý Lead để có kết quả chính xác nhất!";
        }

        Map<String, String> response = new HashMap<>();
        response.put("response", responseText);
        return ResponseEntity.ok(response);
    }
}
