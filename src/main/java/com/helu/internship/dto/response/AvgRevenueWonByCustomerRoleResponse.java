package com.helu.internship.dto.response;

public interface AvgRevenueWonByCustomerRoleResponse {
    String getCustomerRole();

    Long getWonLead();

    Double getTotalRevenue();

    Double getAvgRevenueWon();
}
