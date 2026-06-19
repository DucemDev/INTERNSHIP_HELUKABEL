package com.helu.internship.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class UnderServedSummaryResponse {
    private List<UnderServedSegmentResponse> customers;
    private BigDecimal totalWonRevenue;
    private String topCustomerName;
    private BigDecimal topCustomerShare;
    private BigDecimal top3ConcentrationRatio;
}
