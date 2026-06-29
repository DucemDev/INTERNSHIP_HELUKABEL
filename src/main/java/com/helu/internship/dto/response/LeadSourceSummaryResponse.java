package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface LeadSourceSummaryResponse {
    String getSourceName();
    Long getTotalLeads();
    Long getWonLeads();
    Double getConversionRate();
    BigDecimal getTotalCost();
    BigDecimal getTotalRevenue();
    BigDecimal getAvgCostPerLead();
    BigDecimal getAvgRevenuePerWon();
    Double getRoi();
}
