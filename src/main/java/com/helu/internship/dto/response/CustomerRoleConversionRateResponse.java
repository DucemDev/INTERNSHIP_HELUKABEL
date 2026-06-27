package com.helu.internship.dto.response;

public interface CustomerRoleConversionRateResponse {
    String getCustomerRole();

    Long getTotalLead();

    Long getWonLead();

    Double getConversionRate();
}
