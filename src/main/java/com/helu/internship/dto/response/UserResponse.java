package com.helu.internship.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UUID userId;
    private String userCode;
    private String fullName;
    private String email;
    private Integer roleId;
    private String roleName;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
