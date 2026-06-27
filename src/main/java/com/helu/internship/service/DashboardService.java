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

    List<LostBySellerProjection> getLostBySeller();

    List<LostBySourceProjection> getLostBySource();

    List<LostByRegionProjection> getLostByRegion();

    List<LostByIndustryProjection> getLostByIndustry();

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
    List<SalesOwnerDashboardProjection> getSalesOwnerDashboard();
    RevenueSummaryProjection getRevenueSummary();

    List<RevenueRegionProjection> getRevenueByRegion();

    List<RevenueProductLineProjection> getRevenueByProductLine();
    List<LeadSourceCostProjection> getLeadSourceCostDashboard();

    ConversionRateResponse getStaffStats(String email);

    List<PipelineCoverageProjection> getStaffPipelineCoverage(String email);

    List<RevenueMonthlyProjection> getRevenueMonthly();

    List<RevenueQuarterlyProjection> getRevenueQuarterly();

    List<LeadMonthlyProjection> getLeadMonthly();

    List<LeadQuarterlyProjection> getLeadQuarterly();

    List<RevenueSellerMonthlyProjection> getRevenueSellerMonthly();

    List<RevenueSourceMonthlyProjection> getRevenueSourceMonthly();

    List<RevenueRegionMonthlyProjection> getRevenueRegionMonthly();

    List<RevenueIndustryMonthlyProjection> getRevenueIndustryMonthly();

    List<RevenueProductLineMonthlyProjection> getRevenueProductLineMonthly();

    List<LeadStatusCountResponse> getSellerLeadsByStatusCount(String email);
}
