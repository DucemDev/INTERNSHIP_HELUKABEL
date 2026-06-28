package com.helu.internship.dto.response;

public interface RegionConversionRateResponse {
    String getRegion();

    Long getTotalLead();

    Long getWonLead();

    Double getConversionRate();
}
