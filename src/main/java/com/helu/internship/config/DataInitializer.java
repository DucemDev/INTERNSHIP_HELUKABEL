package com.helu.internship.config;

import com.helu.internship.entity.UserEntity;
import com.helu.internship.repo.RoleRepo;
import com.helu.internship.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepo roleRepo;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // Tự động kiểm tra và thêm các cột thiếu trong bảng lead
        String[] columns = {
            "email VARCHAR(100) NULL",
            "phone_number VARCHAR(20) NULL",
            "product_name VARCHAR(100) NULL",
            "source_name VARCHAR(100) NULL"
        };

        for (String col : columns) {
            String colName = col.split(" ")[0];
            try {
                jdbcTemplate.execute("ALTER TABLE lead ADD " + col);
                System.out.println("Added missing column '" + colName + "' to 'lead' table.");
            } catch (Exception e) {
                // Bỏ qua nếu cột đã tồn tại
            }
        }

        // Tự động sao chép/cập nhật dữ liệu cho các cột mới thêm nếu chúng bị NULL
        try {
            jdbcTemplate.execute(
                "UPDATE lead " +
                "SET source_name = ( " +
                "    SELECT MAX(source_name) " +
                "    FROM lead_source " +
                "    WHERE lead_source.source_id = lead.source_id " +
                ") " +
                "WHERE source_name IS NULL"
            );
            System.out.println("Synchronized 'source_name' values in 'lead' table.");
        } catch (Exception e) {
            System.err.println("Failed to sync source_name: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute(
                "UPDATE lead " +
                "SET product_name = ( " +
                "    SELECT MAX(p.product_name) " +
                "    FROM lead_item li " +
                "    JOIN product p ON li.product_id = p.product_id " +
                "    WHERE li.lead_id = lead.lead_id " +
                ") " +
                "WHERE product_name IS NULL"
            );
            System.out.println("Synchronized 'product_name' values in 'lead' table.");
        } catch (Exception e) {
            System.err.println("Failed to sync product_name: " + e.getMessage());
        }

        // Khớp ngày tạo của lead và lịch sử trạng thái về năm 2026 một cách ĐƠN TRỊ (Idempotent / Deterministic)
        // Dựa vào 4 chữ số cuối của lead_id để phân bổ tháng (1-6) và ngày (1-28) cố định, không bị thay đổi khi restart nhiều lần.
        try {
            jdbcTemplate.execute(
                "UPDATE lead " +
                "SET created_date = DATEFROMPARTS( " +
                "    2026, " +
                "    (CAST(RIGHT(lead_id, 4) AS INT) % 6) + 1, " +
                "    (CAST(RIGHT(lead_id, 4) AS INT) % 28) + 1 " +
                ")"
            );
            jdbcTemplate.execute(
                "UPDATE h " +
                "SET h.changed_at = DATEFROMPARTS( " +
                "    2026, " +
                "    MONTH(l.created_date), " +
                "    (CAST(RIGHT(h.lead_id, 4) AS INT) % 28) + 1 " +
                ") " +
                "FROM lead_status_history h " +
                "JOIN lead l ON h.lead_id = l.lead_id"
            );
            System.out.println("Distributed lead and history dates deterministically into 2026 Q1-Q2 periods.");
        } catch (Exception e) {
            System.err.println("Failed to distribute lead dates: " + e.getMessage());
        }

        // Đổi tên role 'Staff' thành 'Seller' nếu tồn tại
        roleRepo.findByRoleName("Staff").ifPresent(role -> {
            role.setRoleName("Seller");
            roleRepo.save(role);
            System.out.println("Renamed role 'Staff' to 'Seller'");
        });

        List<UserEntity> users = userRepo.findAll();
        for (UserEntity user : users) {
            // Kiểm tra nếu mật khẩu chưa được mã hóa (không bắt đầu bằng $2a$ - tiền tố của BCrypt)
            if (!user.getPassword().startsWith("$2a$")) {
                String rawPassword = user.getPassword();
                user.setPassword(passwordEncoder.encode(rawPassword));
                userRepo.save(user);
                System.out.println("Encoded password for user: " + user.getEmail());
            }
        }
    }
}
