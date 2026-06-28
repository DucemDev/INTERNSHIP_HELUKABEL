package com.helu.internship.dto.response;

public interface AvgRevenueWonByRegionResponse {
    String getRegion();

    Long getWonLead();

    Double getTotalRevenue();

    Double getAvgRevenueWon();
}
