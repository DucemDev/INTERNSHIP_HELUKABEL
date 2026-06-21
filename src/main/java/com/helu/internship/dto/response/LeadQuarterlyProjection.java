package com.helu.internship.dto.response;

public interface LeadQuarterlyProjection {

    Integer getYear();

    Integer getQuarter();

    Long getTotalLead();

    Long getWonLead();

    Long getLostLead();
}