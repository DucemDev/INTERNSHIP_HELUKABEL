package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface RevenueByAccountResponse {
    String getAccount();

    Long getWonLead();

    BigDecimal getTotalRevenue();
}
