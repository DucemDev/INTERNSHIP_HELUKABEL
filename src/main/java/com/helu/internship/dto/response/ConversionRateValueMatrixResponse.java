package com.helu.internship.dto.response;

public interface ConversionRateValueMatrixResponse {
    String getSegmentName();

    String getIndustry();

    String getCustomerRole();

    String getRegion();

    Long getTotalLeads();

    Long getWonLeads();

    Long getLostLeads();

    Double getConversionRate();

    Double getRevenueWon();

    Double getAvgRevenuePerWon();

    String getTopProductLine();

    String getTopLossReason();

    String getTopSalesOwner();
}
