package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface UnderServedSegmentProjection {
    String getSegmentName();
    Long getTotalLeads();
    Long getWonLeads();
    Long getLostLeads();
    BigDecimal getTotalRevenue();
}
