package com.helu.internship.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class UnderServedSegmentResponse {
    private String segmentName;
    private Long totalLeads;
    private Long wonLeads;
    private Long lostLeads;
    private BigDecimal winRate;
    private BigDecimal totalRevenue;
    private BigDecimal revenuePerLead;
    private BigDecimal opportunityScore;
}
