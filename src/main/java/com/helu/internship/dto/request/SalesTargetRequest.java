package com.helu.internship.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesTargetRequest {
    private UUID userId;
    private Integer periodMonth;
    private Integer periodYear;
    private BigDecimal revenueTarget;
}
