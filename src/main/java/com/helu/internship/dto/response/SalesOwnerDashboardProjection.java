package com.helu.internship.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public interface SalesOwnerDashboardProjection {
    UUID getUserId();
    String getUserName();
    Long getTotalLead();
    Long getWonLead();
    Long getOpenLead();
    BigDecimal getTotalRevenue();
    BigDecimal getWinRate();
    BigDecimal getAvgDaysToWon();
}