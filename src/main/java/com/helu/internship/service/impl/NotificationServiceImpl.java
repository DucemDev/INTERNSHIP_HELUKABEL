package com.helu.internship.service.impl;

import com.helu.internship.dto.response.NotificationResponse;
import com.helu.internship.entity.NotificationEntity;
import com.helu.internship.entity.UserEntity;
import com.helu.internship.repo.NotificationRepo;
import com.helu.internship.repo.UserRepo;
import com.helu.internship.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepo notificationRepo;
    private final UserRepo userRepo;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsForUser(String email) {
        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        return notificationRepo.findByRecipientOrderByCreatedAtDesc(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return notificationRepo.countByRecipientAndIsReadFalse(user);
    }

    @Override
    @Transactional
    public void markAsRead(UUID id, String email) {
        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        NotificationEntity notification = notificationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));

        if (!notification.getRecipient().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("You do not have permission to modify this notification");
        }

        notificationRepo.markAsRead(id);
    }

    @Override
    @Transactional
    public void markAllAsRead(String email) {
        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        notificationRepo.markAllAsRead(user);
    }

    @Override
    @Transactional
    public void createNotification(String recipientEmail, String title, String message, String type, String link) {
        UserEntity recipient = userRepo.findByEmail(recipientEmail)
                .orElseThrow(() -> new RuntimeException("Recipient not found with email: " + recipientEmail));

        NotificationEntity notification = NotificationEntity.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .type(type)
                .link(link)
                .isRead(false)
                .build();

        notificationRepo.save(notification);
    }

    @Override
    @Transactional
    public void createNotificationToAdmins(String title, String message, String type, String link) {
        List<UserEntity> admins = userRepo.findByRole_RoleName("Admin");
        for (UserEntity admin : admins) {
            NotificationEntity notification = NotificationEntity.builder()
                    .recipient(admin)
                    .title(title)
                    .message(message)
                    .type(type)
                    .link(link)
                    .isRead(false)
                    .build();
            notificationRepo.save(notification);
        }
    }

    private NotificationResponse mapToResponse(NotificationEntity entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .isRead(entity.getIsRead())
                .type(entity.getType())
                .link(entity.getLink())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
