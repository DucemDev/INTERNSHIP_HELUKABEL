package com.helu.internship.dto.response;

public interface SalesOwnerDetailResponse {
    Long getQualifiedLeads();

    Long getWonLeads();

    Long getLostLeads();

    Double getWinRate();

    Double getAvgDealSize();

    Double getAvgSalesCycle();
}
