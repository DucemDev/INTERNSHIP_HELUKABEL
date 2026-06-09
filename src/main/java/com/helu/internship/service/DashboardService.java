package com.helu.internship.service;

import com.helu.internship.dto.response.LeadSourceCostProjection;
import com.helu.internship.dto.response.PipelineCoverageProjection;

import java.util.List;

public interface DashboardService {
    Double getAverageDaysToWon();
    List<LeadSourceCostProjection> getLeadSourceCostDashboard();
    List<PipelineCoverageProjection> getPipelineCoverage();
}
