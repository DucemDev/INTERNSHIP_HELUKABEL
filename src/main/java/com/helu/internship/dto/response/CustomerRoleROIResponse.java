package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface CustomerRoleROIResponse {
    String getCustomerRole();

    Long getTotalLead();

    BigDecimal getRevenueWon();

    BigDecimal getTotalCost();

    Double getRoi();
}
