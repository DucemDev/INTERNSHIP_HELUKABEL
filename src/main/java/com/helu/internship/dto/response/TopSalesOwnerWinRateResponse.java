package com.helu.internship.dto.response;

public interface TopSalesOwnerWinRateResponse {
    String getUserCode();

    String getFullName();

    Long getTotalLead();

    Long getWonLead();

    Double getWinRate();

}
