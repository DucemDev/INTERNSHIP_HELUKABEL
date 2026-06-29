package com.helu.internship.dto.response;

public interface CustomerValueMatrixResponse {
    String getCustomerGroup(); // legend
    String getSegmentName();   // details
    Double getWinRate();       // X-axis
    Double getAvgRevenuePerWonLead(); // Y-axis
    Double getRevenueWon();    // bubble size
}
