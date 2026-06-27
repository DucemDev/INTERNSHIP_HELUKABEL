package com.helu.internship.config;

import com.helu.internship.entity.UserEntity;
import com.helu.internship.repo.RoleRepo;
import com.helu.internship.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    private final RoleRepo roleRepo;

    @Override
    public void run(String... args) throws Exception {
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
