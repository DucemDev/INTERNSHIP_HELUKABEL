package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface RevenueSummaryProjection {

    BigDecimal getTotalRevenue();

    Long getWonLead();

    BigDecimal getAvgRevenuePerWonLead();
}