package com.helu.internship.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadResponse {
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
    private BigDecimal expectedRevenue;
    private String productName;
    private String sourceName;
    private String userName;

    // BANT Points and Heat Classification
    private Integer bantBudget;
    private Integer bantAuthority;
    private Integer bantNeed;
    private Integer bantTimeline;
    private Integer bantTotalScore;
    private String leadHeat;
}
