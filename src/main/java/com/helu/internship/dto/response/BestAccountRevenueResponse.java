package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface BestAccountRevenueResponse {
    String getAccount();

    Long getWonLead();

    BigDecimal getTotalRevenue();
}
