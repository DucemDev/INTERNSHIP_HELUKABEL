package com.helu.internship.dto.response;

public interface RevenueProductLineMonthlyProjection {

    Integer getYear();

    Integer getMonth();

    String getProductLine();

    Double getRevenue();
}