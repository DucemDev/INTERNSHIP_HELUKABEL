package com.helu.internship.dto.response;

public interface CostPerWinBySourceResponse {
    String getSourceId();
    String getSourceName();
    Double getTotalCost();
    Long getWonLead();
    Double getCostPerWin();
}
