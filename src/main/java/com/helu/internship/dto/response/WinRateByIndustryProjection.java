package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface WinRateByIndustryProjection {
    String getIndustryType();

    Long getQualifiedLead();

    Long getWonLead();

    BigDecimal getWinRate();
}
