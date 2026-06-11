package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface WinRateByRegionProjection {
    String getRegion();

    Long getQualifiedLead();

    Long getWonLead();

    BigDecimal getWinRate();
}
