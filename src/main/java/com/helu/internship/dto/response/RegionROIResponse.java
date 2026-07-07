package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface RegionROIResponse {
    String getRegion();

    Long getTotalLead();

    BigDecimal getRevenueWon();

    BigDecimal getTotalCost();

    Double getRoi();
}
