package com.helu.internship.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesTargetResponse {
    private Long targetId;
    private UUID userId;
    private String userCode;
    private String fullName;
    private Integer periodMonth;
    private Integer periodYear;
    private BigDecimal revenueTarget;
    private UUID createdById;
    private String createdByName;
    private LocalDateTime createdAt;
}
