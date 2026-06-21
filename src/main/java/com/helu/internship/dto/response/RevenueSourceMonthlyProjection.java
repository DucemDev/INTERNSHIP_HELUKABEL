package com.helu.internship.dto.response;

public interface RevenueSourceMonthlyProjection {

    Integer getYear();

    Integer getMonth();

    String getLeadSource();

    Double getRevenue();
}
