package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface RoiLeadSourceResponse {
    String getLabel();
    Long getWonLead();
    BigDecimal getTotalCost();
    BigDecimal getTotalWonValue();
    Double getRoi();
}
