package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface CustomerSegmentPipelineResponse {
    String getSegment();

    Long getTotalLeads();

    Long getQualified();

    Long getProposalSent();

    Long getInNegotiation();

    Long getWon();

    Long getLost();

    BigDecimal getPipelineValue();

    Double getWinRate();
}
