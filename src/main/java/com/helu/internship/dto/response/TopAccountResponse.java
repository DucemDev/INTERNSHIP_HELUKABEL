package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface TopAccountResponse {
    String getAccountName();

    BigDecimal getRevenueWon();

    BigDecimal getLostOpportunityValue();

    BigDecimal getOpenPipelineValue();

    BigDecimal getTotalOpportunity();

}
