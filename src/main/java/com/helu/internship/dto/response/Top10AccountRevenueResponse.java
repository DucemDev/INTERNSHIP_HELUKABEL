package com.helu.internship.dto.response;

public interface Top10AccountRevenueResponse {
    String getAccount();

    String getIndustry();

    String getProductLine();

    String getCustomerGroup();

    String getRegion();

    String getCustomerRole();

    Double getCostPerLead();

    Double getRevenue();
}
