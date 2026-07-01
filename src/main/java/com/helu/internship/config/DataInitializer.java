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
        // Tự động sửa cấu trúc bảng notification nếu sai schema (dùng notification_id thay vì id)
        try {
            jdbcTemplate.execute("SELECT TOP 1 notification_id FROM notification");
            System.out.println("Detected old notification table schema (using BIGINT notification_id). Recreating it to match NotificationEntity (UUID id)...");
            try {
                jdbcTemplate.execute("ALTER TABLE notification DROP CONSTRAINT FK_notification_user");
            } catch (Exception ignored) {}
            try {
                jdbcTemplate.execute("DROP TABLE notification");
            } catch (Exception ignored) {}
        } catch (Exception e) {
            // Bảng chưa có hoặc đã dùng đúng schema UUID
        }

        try {
            jdbcTemplate.execute(
                "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'notification') " +
                "BEGIN " +
                "    CREATE TABLE notification ( " +
                "        id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL, " +
                "        user_id UNIQUEIDENTIFIER NOT NULL, " +
                "        title NVARCHAR(200) NOT NULL, " +
                "        message NVARCHAR(1000) NOT NULL, " +
                "        is_read BIT DEFAULT 0 NOT NULL, " +
                "        type VARCHAR(50) NOT NULL, " +
                "        link VARCHAR(255) NULL, " +
                "        created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL, " +
                "        CONSTRAINT PK_notification PRIMARY KEY (id), " +
                "        CONSTRAINT FK_notification_user FOREIGN KEY (user_id) REFERENCES [user](user_id) ON DELETE CASCADE " +
                "    ); " +
                "    PRINT 'Recreated notification table successfully.'; " +
                "END"
            );
        } catch (Exception e) {
            System.err.println("Failed to create correct notification table: " + e.getMessage());
        }

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
