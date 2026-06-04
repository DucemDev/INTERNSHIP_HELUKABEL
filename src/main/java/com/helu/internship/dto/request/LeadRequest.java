package com.helu.internship.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadRequest {
    private String leadId;
    private LocalDate createdDate;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String account;
    private String industryType;
    private String customerGroup;
    private String customerRole;
    private String location;
    private String region;
    private String status;
    private BigDecimal cost;
    private String lossReason;
    private BigDecimal businessResult;
    private String productId;
    private String sourceId;
    private UUID userId;
}
