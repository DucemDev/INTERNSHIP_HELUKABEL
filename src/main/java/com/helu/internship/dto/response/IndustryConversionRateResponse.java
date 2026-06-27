package com.helu.internship.dto.response;

public interface IndustryConversionRateResponse {
    String getIndustry();

    Long getTotalLead();

    Long getWonLead();

    Double getConversionRate();
}
