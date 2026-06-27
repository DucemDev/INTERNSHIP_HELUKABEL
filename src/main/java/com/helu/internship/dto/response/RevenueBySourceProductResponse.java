package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface RevenueBySourceProductResponse {
    String getSourceName();

    String getProductLine();

    BigDecimal getTotalRevenue();
}
