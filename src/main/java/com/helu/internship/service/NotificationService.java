package com.helu.internship.service;

import com.helu.internship.dto.response.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<NotificationResponse> getNotificationsForUser(String email);

    long getUnreadCount(String email);

    void markAsRead(UUID id, String email);

    void markAllAsRead(String email);

    void createNotification(String recipientEmail, String title, String message, String type, String link);

    void createNotificationToAdmins(String title, String message, String type, String link);
}
