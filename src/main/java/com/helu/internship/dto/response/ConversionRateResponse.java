package com.helu.internship.dto.response;

import java.time.LocalDate;

public interface ConversionRateResponse {
    Long getTotalLead();
    Long getWonLead();
    String getLabel();
    String getRegion();
    String getCustomerGroup();
    LocalDate getTimeFrom();
    LocalDate getTimeTo();
    Double getConversionRate();
}
