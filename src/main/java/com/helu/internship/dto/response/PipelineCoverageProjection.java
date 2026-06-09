package com.helu.internship.dto.response;

import java.math.BigDecimal;

public interface PipelineCoverageProjection {
    String getUserCode();

    String getSalesOwner();

    BigDecimal getOpenPipeline();

    BigDecimal getTarget();

    BigDecimal getPipelineCoverage();
}
