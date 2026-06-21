package com.helu.internship.dto.response;

public interface RevenueSellerMonthlyProjection {

    Integer getYear();

    Integer getMonth();

    String getSellerName();

    Double getRevenue();
}