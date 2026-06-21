package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface PipelineCoverageProjection {

    String getUserCode();

    String getSalesOwner();

    BigDecimal getOpenPipeline();

    BigDecimal getTargetRevenue();

    BigDecimal getPipelineCoverage();

    Integer getPeriodMonth();

    Integer getPeriodYear();
}
