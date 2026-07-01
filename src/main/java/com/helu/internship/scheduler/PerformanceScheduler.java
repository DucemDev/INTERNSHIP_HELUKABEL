package com.helu.internship.scheduler;

import com.helu.internship.dto.response.PerformanceStatsProjection;
import com.helu.internship.entity.UserEntity;
import com.helu.internship.repo.LeadRepo;
import com.helu.internship.repo.UserRepo;
import com.helu.internship.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PerformanceScheduler {

    private final UserRepo userRepo;
    private final LeadRepo leadRepo;
    private final NotificationService notificationService;

    // Chạy vào lúc 0h00 sáng mỗi ngày
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkSellerPerformanceDaily() {
        log.info("Bắt đầu kiểm tra hiệu suất seller định kỳ...");
        checkPerformance();
        log.info("Hoàn thành kiểm tra hiệu suất seller.");
    }

    @Transactional
    public void checkPerformance() {
        List<UserEntity> sellers = userRepo.findByRole_RoleName("Seller");
        LocalDate sinceDate = LocalDate.now().minusDays(30);

        for (UserEntity seller : sellers) {
            PerformanceStatsProjection stats = leadRepo.getPerformanceStatsForUser(seller.getUserId(), sinceDate);
            if (stats == null || stats.getTotalLeads() == 0) {
                continue; // Không có lead nào trong 30 ngày qua
            }

            long total = stats.getTotalLeads();
            long won = stats.getWonLeads();
            long lost = stats.getLostLeads();

            double winRate = (double) won * 100.0 / total;
            double lossRate = (double) lost * 100.0 / total;

            boolean lowWinRate = winRate < 30.0;
            boolean highLossRate = lossRate > 50.0;

            if (lowWinRate || highLossRate) {
                String title = "Cảnh báo: Hiệu suất hoạt động thấp";
                String message = String.format(
                        "Seller %s có hiệu suất thấp trong 30 ngày qua: Tỉ lệ thắng (Win Rate): %.1f%% (Yêu cầu >= 30%%), Tỉ lệ mất (Loss Rate): %.1f%% (Yêu cầu <= 50%%). Tổng số Lead: %d.",
                        seller.getFullName(), winRate, lossRate, total
                );

                // Gửi thông báo cho Seller tự cải thiện
                notificationService.createNotification(
                        seller.getEmail(),
                        title,
                        message,
                        "LOW_PERFORMANCE",
                        "/seller/dashboard"
                );

                // Gửi thông báo cho các Admins theo dõi
                notificationService.createNotificationToAdmins(
                        "Cảnh báo: Hiệu suất thấp (" + seller.getFullName() + ")",
                        message,
                        "LOW_PERFORMANCE",
                        "/dashboard"
                );
            }
        }
    }
}
