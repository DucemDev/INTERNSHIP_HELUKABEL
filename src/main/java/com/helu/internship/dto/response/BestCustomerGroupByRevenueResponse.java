package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface BestCustomerGroupByRevenueResponse {
    String getCustomerGroup();

    BigDecimal getTotalRevenue();
}
