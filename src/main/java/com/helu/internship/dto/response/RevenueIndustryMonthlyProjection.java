package com.helu.internship.dto.response;

public interface RevenueIndustryMonthlyProjection {

    Integer getYear();

    Integer getMonth();

    String getIndustry();

    Double getRevenue();
}
