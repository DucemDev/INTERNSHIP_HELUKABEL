package com.helu.internship.service;


import com.helu.internship.dto.response.*;

import java.util.List;

public interface DashboardService {

    Double getAverageDaysToWon();

    List<LeadSourceCostProjection> getLeadSourceCostDashboard();

    List<PipelineCoverageProjection> getPipelineCoverage();

    List<LeadStatusCountResponse> getLeadStatusCount();

    List<LeadByStatusResponse> getLeadsByStatus(String status);

    ConversionRateResponse getConversionRate();

    List<WinRateBySalesResponse> getWinRateBySalesOwner();

    List<WinRateByIndustryProjection> getWinRateByIndustry();

    List<WinRateByRegionProjection> getWinRateByRegion();

    List<CostPerWinBySourceResponse> getCostPerWinByLeadSource();
}

