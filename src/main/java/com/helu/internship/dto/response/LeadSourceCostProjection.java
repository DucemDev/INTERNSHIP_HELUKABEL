package com.helu.internship.dto.response;

import java.math.BigDecimal;
public interface LeadSourceCostProjection {
    String getSourceId();

    String getSourceName();

    BigDecimal getTotalCost();

    Long getTotalLead();

    BigDecimal getCostPerLead();
}
