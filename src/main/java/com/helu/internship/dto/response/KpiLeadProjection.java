package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface KpiLeadProjection {
    String getLeadId();
    String getFullName();
    String getCompanyName();
    String getStatus();
    BigDecimal getRevenue();
    String getWonDate();
}
