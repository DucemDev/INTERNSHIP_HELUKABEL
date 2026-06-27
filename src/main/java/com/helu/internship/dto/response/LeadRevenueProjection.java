package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface LeadRevenueProjection {
    String getLeadId();
    BigDecimal getExpectedRevenue();
}
