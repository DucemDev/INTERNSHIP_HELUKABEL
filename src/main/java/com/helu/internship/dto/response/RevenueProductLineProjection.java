package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface RevenueProductLineProjection {

    String getProductId();

    String getProductName();

    BigDecimal getRevenue();

    Long getTotalWonLead();
}