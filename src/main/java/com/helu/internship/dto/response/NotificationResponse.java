package com.helu.internship.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private UUID id;
    private String title;
    private String message;
    private Boolean isRead;
    private String type;
    private String link;
    private LocalDateTime createdAt;
}
