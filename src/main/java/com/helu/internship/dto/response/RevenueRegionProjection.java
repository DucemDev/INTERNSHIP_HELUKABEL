package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface RevenueRegionProjection {

    String getRegion();

    BigDecimal getRevenue();

    Long getWonLead();
}