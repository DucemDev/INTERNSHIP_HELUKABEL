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
    List<LeadSourceSummaryResponse> getLeadSourceSummary();


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
    List<SalesOwnerDashboardProjection> getSalesOwnerDashboardByQuarter(String quarter);
    List<WinRateBySalesResponse> getWinRateBySalesOwnerByQuarter(String quarter);
    List<PipelineCoverageProjection> getPipelineCoverageByQuarter(String quarter);
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

    List<RevenueByAccountResponse> getRevenueByAccount();
    List<LeadByIndustryResponse> getPotentialLeadByIndustry();
    List<LeadFunnelBySourceResponse> getLeadFunnelBySource(String sourceId);
    List<LeadSourceByProductProjection> getLeadSourceByProduct();
    List<RevenueBySourceProductResponse> getRevenueBySourceProduct();
    List<WonLeadBySourceProductResponse> getWonLeadBySourceProduct();
    List<LostLeadBySourceResponse> getLostLeadBySource();
    BestAccountRevenueResponse getBestAccountByRevenue();
    BestIndustryByWonDealResponse getBestIndustryByWonDeal();
    BestIndustryByRevenueResponse getBestIndustryByRevenue();
    BestRegionByWonDealResponse getBestRegionByWonDeal();
    BestRegionByRevenueResponse getBestRegionByRevenue();
    BestCustomerGroupByLeadResponse getBestCustomerGroupByLead();
    BestCustomerGroupByRevenueResponse getBestCustomerGroupByRevenue();
    TotalLeadByIndustryResponse getTotalLeadByIndustry(String industry);
    List<WonLeadByIndustryResponse> getWonLeadByIndustry();
    List<IndustryConversionRateResponse> getIndustryConversionRate();
    List<AvgSalesCycleByIndustryResponse> getAvgSalesCycleByIndustry();
    List<BestLostReasonByIndustryResponse> getBestLostReasonByIndustry();
    List<TotalLeadByCustomerRoleResponse> getTotalLeadByCustomerRole();
    List<RevenueByCustomerRoleResponse> getRevenueByCustomerRole();
    List<WonLeadByCustomerRoleResponse> getWonLeadByCustomerRole();
    List<CustomerRoleConversionRateResponse> getCustomerRoleConversionRate();
    List<AvgRevenueWonByCustomerRoleResponse> getAvgRevenueWonByCustomerRole();
    List<LostLeadByCustomerRoleResponse> getLostLeadByCustomerRole();
    List<BestLostReasonByCustomerRoleResponse> getBestLostReasonByCustomerRole();
    List<AvgSalesCycleByCustomerRoleResponse> getAvgSalesCycleByCustomerRole();
    List<TotalLeadByRegionResponse> getTotalLeadByRegion();
    List<WonLeadByRegionResponse> getWonLeadByRegion();
    List<RegionConversionRateResponse> getRegionConversionRate();
    List<AvgRevenueWonByRegionResponse> getAvgRevenueWonByRegion();
    List<LostLeadByRegionResponse> getLostLeadByRegion();
    List<BestLostReasonByRegionResponse> getBestLostReasonByRegion();
    List<TotalLeadByProductLineResponse> getTotalLeadByProductLine();
    List<WonLeadByProductLineResponse> getWonLeadByProductLine();
    List<ProductLineConversionRateResponse> getProductLineConversionRate();
    List<AvgRevenueWonByProductLineResponse> getAvgRevenueWonByProductLine();
    List<LostLeadByProductLineResponse> getLostLeadByProductLine();
    List<BestLostReasonByProductLineResponse> getBestLostReasonByProductLine();
    List<CustomerGroupROIResponse> getCustomerGroupROI();
    List<CustomerGroupCPLResponse> getCustomerGroupCostPerLead();
    List<Top10AccountRevenueResponse> getTop10Accounts();
    TopSalesOwnerRevenueResponse getTopSalesOwnerRevenue();
    TopSalesOwnerWinRateResponse getTopSalesOwnerWinRate();
    FastestSalesOwnerResponse getFastestSalesOwner();
    List<SalesOwnerAvgSalesCycleResponse> getSalesOwnerAvgSalesCycle();
    List<SalesOwnerBantCompleteRateResponse> getSalesOwnerBantCompleteRate();
    List<SalesOwnerAvgBantScoreResponse> getSalesOwnerAvgBantScore();
    List<LossReasonByProductLineResponse> getLossReasonByProductLine();
    List<CustomerValueMatrixResponse> getCustomerValueMatrix();

    // Retrieves data for Customer Value Matrix chart
    SalesOwnerDetailResponse getSalesOwnerDetail(String userCode);
    List<com.helu.internship.entity.LeadSourceEntity> getAllLeadSources();
}

