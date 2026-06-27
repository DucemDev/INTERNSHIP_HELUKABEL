package com.helu.internship.dto.response;

public interface LostLeadBySourceResponse {
    String getSourceName();

    String getProductName();

    String getLossReason();

    Long getTotalLost();
}
