package com.helu.internship.service;


import com.helu.internship.dto.response.ConversionRateResponse;
import com.helu.internship.dto.response.CostPerWinBySourceResponse;

import com.helu.internship.dto.response.LeadByStatusResponse;
import com.helu.internship.dto.response.LeadSourceCostProjection;
import com.helu.internship.dto.response.LeadStatusCountResponse;
import com.helu.internship.dto.response.PipelineCoverageProjection;
import com.helu.internship.dto.response.WinRateBySalesResponse;

import java.util.List;

public interface DashboardService {

    Double getAverageDaysToWon();

    List<LeadSourceCostProjection> getLeadSourceCostDashboard();

    List<PipelineCoverageProjection> getPipelineCoverage(String sellerCode);

    List<LeadStatusCountResponse> getLeadStatusCount();

    List<LeadByStatusResponse> getLeadsByStatus(String status);

    ConversionRateResponse getConversionRate();

    List<WinRateBySalesResponse> getWinRateBySalesOwner();

    List<CostPerWinBySourceResponse> getCostPerWinByLeadSource();
}

