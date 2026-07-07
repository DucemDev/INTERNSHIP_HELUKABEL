package com.helu.internship.dto.response;

public interface ProductLineROIResponse {
    String getProductName();

    Long getTotalLead();

    Double getRevenueWon();

    Double getCostPerLead();

    Double getTotalCost();

    Double getRoi();
}
