package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface BestRegionByRevenueResponse {
    String getRegion();

    BigDecimal getTotalRevenue();
}
