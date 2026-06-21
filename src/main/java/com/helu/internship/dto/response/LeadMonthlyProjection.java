package com.helu.internship.dto.response;

public interface LeadMonthlyProjection {

    Integer getYear();

    Integer getMonth();

    Long getTotalLead();

    Long getWonLead();

    Long getLostLead();
}