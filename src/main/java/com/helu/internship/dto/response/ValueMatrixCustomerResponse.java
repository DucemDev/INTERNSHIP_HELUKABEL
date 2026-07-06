package com.helu.internship.dto.response;

public interface ValueMatrixCustomerResponse {
    String getIndustry();

    String getCustomerRole();

    String getRegion();

    String getSegmentName();

    Long getTotalLeads();

    Long getWonLeads();

    Long getLostLeads();

    Double getRevenueWon();

    Double getAvgRevenuePerWon();

    Double getWinRate();
}
