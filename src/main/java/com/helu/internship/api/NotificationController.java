package com.helu.internship.api;

import com.helu.internship.dto.response.NotificationResponse;
import com.helu.internship.scheduler.PerformanceScheduler;
import com.helu.internship.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final PerformanceScheduler performanceScheduler;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(notificationService.getNotificationsForUser(principal.getName()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(notificationService.getUnreadCount(principal.getName()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        notificationService.markAsRead(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        notificationService.markAllAsRead(principal.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/check-performance")
    public ResponseEntity<String> checkPerformance() {
        performanceScheduler.checkPerformance();
        return ResponseEntity.ok("Performance check triggered successfully");
    }
}
