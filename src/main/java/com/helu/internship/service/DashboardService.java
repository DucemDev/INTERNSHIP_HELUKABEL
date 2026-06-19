package com.helu.internship.service;


import com.helu.internship.dto.response.*;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    Double getAverageDaysToWon();

    List<PipelineCoverageProjection> getPipelineCoverage(String sellerCode);

    List<LeadStatusCountResponse> getLeadStatusCount();

    List<LeadByStatusResponse> getLeadsByStatus(String status);

    ConversionRateResponse getConversionRate();

    List<WinRateBySalesResponse> getWinRateBySalesOwner(String region, String industry);

    List<WinRateByIndustryProjection> getWinRateByIndustry();

    List<WinRateByRegionProjection> getWinRateByRegion();

    List<CostPerWinBySourceResponse> getCostPerWinByLeadSource();

    List<LostReasonSummaryProjection> getLostReasonSummary(String productId);

    List<RevenueIndustryResponse> getRevenueByIndustry();

    List<RoiLeadSourceResponse> getROIByLeadSource();

    List<ConversionRateResponse> getConversionRateFilter(
            String sourceId,
            String sourceType,
            String region,
            String industry,
            String salesOwnerId,
            String customerGroup,
            LocalDate timeFrom,
            LocalDate timeTo
    );

    List<LeadSourceCostProjection> getLeadSourceCostDashboard();

    ConversionRateResponse getStaffStats(String email);

    List<PipelineCoverageProjection> getStaffPipelineCoverage(String email);

}
