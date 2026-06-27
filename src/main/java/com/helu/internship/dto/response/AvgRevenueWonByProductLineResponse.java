package com.helu.internship.dto.response;

public interface AvgRevenueWonByProductLineResponse {
    String getProductName();

    Long getWonLead();

    Double getTotalRevenue();

    Double getAvgRevenueWon();
}
