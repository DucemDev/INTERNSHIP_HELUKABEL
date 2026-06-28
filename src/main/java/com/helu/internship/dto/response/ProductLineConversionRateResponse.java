package com.helu.internship.dto.response;

public interface ProductLineConversionRateResponse {
    String getProductName();

    Long getTotalLead();

    Long getWonLead();

    Double getConversionRate();
}
