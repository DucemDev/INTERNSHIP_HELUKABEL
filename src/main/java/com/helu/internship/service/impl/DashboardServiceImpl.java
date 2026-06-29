package com.helu.internship.service.impl;


import com.helu.internship.dto.response.*;

import com.helu.internship.repo.*;
import com.helu.internship.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final LeadRepo leadRepo;
    private final LeadStatusHistoryRepo leadStatusHistoryRepo;
    private final CostPerLeadRepo costPerLeadRepo;
    private final PipelineCoveragerRepo pipelineCoverageRepo;
    private final UserRepo userRepo;
    private final SalesOwnerDashboardRepo salesOwnerDashboardRepo;
    private final LeadSourceRepo leadSourceRepo;

    public DashboardServiceImpl(
            LeadRepo leadRepo,
            LeadStatusHistoryRepo leadStatusHistoryRepo,
            CostPerLeadRepo costPerLeadRepo,
            PipelineCoveragerRepo pipelineCoverageRepo,
            UserRepo userRepo,
            SalesOwnerDashboardRepo salesOwnerDashboardRepo,
            LeadSourceRepo leadSourceRepo) {

        this.leadRepo = leadRepo;
        this.leadStatusHistoryRepo = leadStatusHistoryRepo;
        this.costPerLeadRepo = costPerLeadRepo;
        this.pipelineCoverageRepo = pipelineCoverageRepo;
        this.userRepo = userRepo;
        this.salesOwnerDashboardRepo = salesOwnerDashboardRepo;
        this.leadSourceRepo = leadSourceRepo;
    }


    @Override
    public List<WinRateByIndustryProjection> getWinRateByIndustry() {
        return leadRepo.getWinRateByIndustry();
    }

    @Override
    public List<WinRateByRegionProjection> getWinRateByRegion() {
        return leadRepo.getWinRateByRegion();
    }

    @Override
    public List<LeadStatusCountResponse> getLeadStatusCount() {
        return leadRepo.countLeadByStatus()
                .stream()
                .map(row -> new LeadStatusCountResponse(
                        String.valueOf(row[0]),
                        (Long) row[1]
                ))
                .toList();
    }

    @Override
    public List<LeadByStatusResponse> getLeadsByStatus(String status) {
        return leadRepo.findLeadsByStatus(status);
    }

    @Override
    public List<LeadSourceCostProjection> getLeadSourceCostDashboard() {
        return costPerLeadRepo.getCostPerLeadBySource();
    }

    @Override
    public Double getAverageDaysToWon() {
        return leadStatusHistoryRepo.getAverageDaysToWon();
    }

    @Override
    public List<PipelineCoverageProjection> getPipelineCoverage(String sellerCode) {
        return pipelineCoverageRepo.getPipelineCoverage(sellerCode);
    }

    @Override
    public ConversionRateResponse getConversionRate() {
        return leadRepo.getConversionRate();
    }

    @Override
    public List<WinRateBySalesResponse> getWinRateBySalesOwner(String region, String industry) {
        return leadRepo.getWinRateBySalesOwner(region, industry);
    }

    @Override
    public List<CostPerWinBySourceResponse> getCostPerWinByLeadSource() {
        return leadRepo.getCostPerWinByLeadSource();
    }

    @Override
    public List<LostReasonSummaryProjection> getLostReasonSummary(String productId) {
        return leadRepo.getLostReasonSummary(productId);
    }

    @Override
    public List<RevenueIndustryResponse> getRevenueByIndustry() {
        return leadRepo.getRevenueByIndustry();
    }
    @Override
    public RevenueSummaryProjection getRevenueSummary() {
        return leadRepo.getRevenueSummary();
    }

    @Override
    public List<RevenueRegionProjection> getRevenueByRegion() {
        return leadRepo.getRevenueByRegion();
    }

    @Override
    public List<RevenueProductLineProjection> getRevenueByProductLine() {
        return leadRepo.getRevenueByProductLine();
    }
    @Override
    public List<ConversionRateResponse> getConversionRateFilter(
            String sourceId,
            String sourceType,
            String region,
            String industry,
            String salesOwnerId,
            String customerGroup,
            LocalDate timeFrom,
            LocalDate timeTo
    ) {
        return leadRepo.getConversionRateFilter(
                sourceId,
                sourceType,
                region,
                industry,
                salesOwnerId,
                customerGroup,
                timeFrom,
                timeTo
        );
    }

    @Override
    public List<RoiLeadSourceResponse> getROIByLeadSource() {
        return leadRepo.getROIByLeadSource();
     }

    @Override
    public List<LeadSourceSummaryResponse> getLeadSourceSummary() {
        return leadRepo.getLeadSourceSummary();
    }


    @Override
    public ConversionRateResponse getStaffStats(String email) {
        return leadRepo.getStatsByEmail(email);
    }

    @Override
    public List<PipelineCoverageProjection> getStaffPipelineCoverage(String email) {
        String userCode = userRepo.findByEmail(email)
                .map(u -> u.getUserCode())
                .orElse(null);
        List<PipelineCoverageProjection> list = pipelineCoverageRepo.getPipelineCoverage(userCode);
        return list.stream()
                .sorted((a, b) -> {
                    int yearComp = Integer.compare(b.getPeriodYear(), a.getPeriodYear());
                    if (yearComp != 0) return yearComp;
                    return Integer.compare(b.getPeriodMonth(), a.getPeriodMonth());
                })
                .toList();
    }
    @Override
    public List<LostBySellerProjection> getLostBySeller() {
        return leadRepo.getLostBySeller();
    }

    @Override
    public List<LostBySourceProjection> getLostBySource() {
        return leadRepo.getLostBySource();
    }

    @Override
    public List<LostByRegionProjection> getLostByRegion() {
        return leadRepo.getLostByRegion();
    }

    @Override
    public List<LostByIndustryProjection> getLostByIndustry() {
        return leadRepo.getLostByIndustry();
    }
    @Override
    public List<SalesOwnerDashboardProjection> getSalesOwnerDashboard() {
        return salesOwnerDashboardRepo.getSalesOwnerDashboard();
    }
    @Override
    public List<SalesOwnerDashboardProjection> getSalesOwnerDashboardByQuarter(String quarter) {
        return salesOwnerDashboardRepo.getSalesOwnerDashboardByQuarter(quarter);
    }
    @Override
    public List<WinRateBySalesResponse> getWinRateBySalesOwnerByQuarter(String quarter) {
        return leadRepo.getWinRateBySalesOwnerByQuarter(quarter);
    }
    @Override
    public List<PipelineCoverageProjection> getPipelineCoverageByQuarter(String quarter) {
        return pipelineCoverageRepo.getPipelineCoverageByQuarter(quarter);
    }
    @Override
    public List<RevenueMonthlyProjection> getRevenueMonthly() {
        return leadRepo.getRevenueMonthly();
    }

    @Override
    public List<RevenueQuarterlyProjection> getRevenueQuarterly() {
        return leadRepo.getRevenueQuarterly();
    }

    @Override
    public List<LeadMonthlyProjection> getLeadMonthly() {
        return leadRepo.getLeadMonthly();
    }

    @Override
    public List<LeadQuarterlyProjection> getLeadQuarterly() {
        return leadRepo.getLeadQuarterly();
    }
    @Override
    public List<RevenueSellerMonthlyProjection> getRevenueSellerMonthly() {
        return leadRepo.getRevenueSellerMonthly();
    }

    @Override
    public List<RevenueSourceMonthlyProjection> getRevenueSourceMonthly() {
        return leadRepo.getRevenueSourceMonthly();
    }

    @Override
    public List<RevenueRegionMonthlyProjection> getRevenueRegionMonthly() {
        return leadRepo.getRevenueRegionMonthly();
    }

    @Override
    public List<RevenueIndustryMonthlyProjection> getRevenueIndustryMonthly() {
        return leadRepo.getRevenueIndustryMonthly();
    }

    @Override
    public List<RevenueProductLineMonthlyProjection> getRevenueProductLineMonthly() {
        return leadRepo.getRevenueProductLineMonthly();
    }

    @Override
    public List<LeadStatusCountResponse> getSellerLeadsByStatusCount(String email) {
        return leadRepo.countLeadByStatusAndSellerEmail(email)
                .stream()
                .map(row -> new LeadStatusCountResponse(
                        String.valueOf(row[0]),
                        (Long) row[1]
                ))
                .toList();
    }

    @Override
    public List<RevenueByAccountResponse> getRevenueByAccount() {
        return leadRepo.getRevenueByAccount();
    }
    @Override
    public List<LeadByIndustryResponse> getPotentialLeadByIndustry() {
        return leadRepo.getPotentialLeadByIndustry();
    }
    @Override
    public List<LeadFunnelBySourceResponse> getLeadFunnelBySource(
            String sourceId
    ) {
        return leadRepo.getLeadFunnelBySource(sourceId);
    }
    @Override
    public List<LeadSourceByProductProjection> getLeadSourceByProduct() {
        return leadRepo.getLeadSourceByProduct();
    }
    @Override
    public List<RevenueBySourceProductResponse> getRevenueBySourceProduct() {
        return leadRepo.getRevenueBySourceProduct();
    }
    @Override
    public List<WonLeadBySourceProductResponse> getWonLeadBySourceProduct() {
        return leadRepo.getWonLeadBySourceProduct();
    }
    @Override
    public List<LostLeadBySourceResponse> getLostLeadBySource() {
        return leadRepo.getLostLeadBySource();
    }
    @Override
    public BestAccountRevenueResponse getBestAccountByRevenue() {
        return leadRepo.getBestAccountByRevenue();
    }
    @Override
    public BestIndustryByWonDealResponse getBestIndustryByWonDeal() {
        return leadRepo.getBestIndustryByWonDeal();
    }
    @Override
    public BestIndustryByRevenueResponse getBestIndustryByRevenue() {
        return leadRepo.getBestIndustryByRevenue();
    }

    @Override
    public BestRegionByWonDealResponse getBestRegionByWonDeal() {
        return leadRepo.getBestRegionByWonDeal();
    }

    @Override
    public BestRegionByRevenueResponse getBestRegionByRevenue() {
        return leadRepo.getBestRegionByRevenue();
    }
    @Override
    public BestCustomerGroupByLeadResponse getBestCustomerGroupByLead() {
        return leadRepo.getBestCustomerGroupByLead();
    }
    @Override
    public BestCustomerGroupByRevenueResponse getBestCustomerGroupByRevenue() {
        return leadRepo.getBestCustomerGroupByRevenue();
    }
    @Override
    public TotalLeadByIndustryResponse getTotalLeadByIndustry(String industry) {
        return leadRepo.getTotalLeadByIndustry(industry);
    }
    @Override
    public List<WonLeadByIndustryResponse> getWonLeadByIndustry() {
        return leadRepo.getWonLeadByIndustry();
    }
    @Override
    public List<IndustryConversionRateResponse> getIndustryConversionRate() {
        return leadRepo.getIndustryConversionRate();
    }
    @Override
    public List<AvgSalesCycleByIndustryResponse> getAvgSalesCycleByIndustry() {
        return leadRepo.getAvgSalesCycleByIndustry();
    }
    @Override
    public List<BestLostReasonByIndustryResponse> getBestLostReasonByIndustry() {
        return leadRepo.getBestLostReasonByIndustry();
    }
    @Override
    public List<TotalLeadByCustomerRoleResponse> getTotalLeadByCustomerRole() {
        return leadRepo.getTotalLeadByCustomerRole();
    }
    @Override
    public List<RevenueByCustomerRoleResponse> getRevenueByCustomerRole() {
        return leadRepo.getRevenueByCustomerRole();
    }
    @Override
    public List<WonLeadByCustomerRoleResponse> getWonLeadByCustomerRole() {
        return leadRepo.getWonLeadByCustomerRole();
    }
    @Override
    public List<CustomerRoleConversionRateResponse> getCustomerRoleConversionRate() {
        return leadRepo.getCustomerRoleConversionRate();
    }
    @Override
    public List<AvgRevenueWonByCustomerRoleResponse> getAvgRevenueWonByCustomerRole() {
        return leadRepo.getAvgRevenueWonByCustomerRole();
    }
    @Override
    public List<LostLeadByCustomerRoleResponse> getLostLeadByCustomerRole() {
        return leadRepo.getLostLeadByCustomerRole();
    }
    @Override
    public List<BestLostReasonByCustomerRoleResponse> getBestLostReasonByCustomerRole() {
        return leadRepo.getBestLostReasonByCustomerRole();
    }
    @Override
    public List<AvgSalesCycleByCustomerRoleResponse> getAvgSalesCycleByCustomerRole() {
        return leadRepo.getAvgSalesCycleByCustomerRole();
    }

    @Override
    public List<TotalLeadByRegionResponse> getTotalLeadByRegion() {
        return leadRepo.getTotalLeadByRegion();
    }
    @Override
    public List<WonLeadByRegionResponse> getWonLeadByRegion() {
        return leadRepo.getWonLeadByRegion();
    }
    @Override
    public List<RegionConversionRateResponse> getRegionConversionRate() {
        return leadRepo.getRegionConversionRate();
    }
    @Override
    public List<AvgRevenueWonByRegionResponse> getAvgRevenueWonByRegion() {
        return leadRepo.getAvgRevenueWonByRegion();
    }
    @Override
    public List<LostLeadByRegionResponse> getLostLeadByRegion() {
        return leadRepo.getLostLeadByRegion();
    }
    @Override
    public List<BestLostReasonByRegionResponse> getBestLostReasonByRegion() {
        return leadRepo.getBestLostReasonByRegion();
    }
    @Override
    public List<TotalLeadByProductLineResponse> getTotalLeadByProductLine() {
        return leadRepo.getTotalLeadByProductLine();
    }
    @Override
    public List<WonLeadByProductLineResponse> getWonLeadByProductLine() {
        return leadRepo.getWonLeadByProductLine();
    }
    @Override
    public List<ProductLineConversionRateResponse> getProductLineConversionRate() {
        return leadRepo.getProductLineConversionRate();
    }
    @Override
    public List<AvgRevenueWonByProductLineResponse> getAvgRevenueWonByProductLine() {
        return leadRepo.getAvgRevenueWonByProductLine();
    }
    @Override
    public List<LostLeadByProductLineResponse> getLostLeadByProductLine() {
        return leadRepo.getLostLeadByProductLine();
    }
    @Override
    public List<BestLostReasonByProductLineResponse> getBestLostReasonByProductLine() {
        return leadRepo.getBestLostReasonByProductLine();
    }
    @Override
    public List<CustomerGroupROIResponse> getCustomerGroupROI() {
        return leadRepo.getCustomerGroupROI();
    }
    @Override
    public List<CustomerGroupCPLResponse> getCustomerGroupCostPerLead() {
        return leadRepo.getCustomerGroupCostPerLead();
    }
    @Override
    public List<Top10AccountRevenueResponse> getTop10Accounts() {
        return leadRepo.getTop10Accounts();
    }
    @Override
    public TopSalesOwnerRevenueResponse getTopSalesOwnerRevenue() {
        return leadRepo.getTopSalesOwnerRevenue();
    }
    @Override
    public TopSalesOwnerWinRateResponse getTopSalesOwnerWinRate() {
        return leadRepo.getTopSalesOwnerWinRate();
    }
    @Override
    public FastestSalesOwnerResponse getFastestSalesOwner() {
        return leadRepo.getFastestSalesOwner();
    }
    @Override
    public List<SalesOwnerAvgSalesCycleResponse> getSalesOwnerAvgSalesCycle() {
        return leadRepo.getSalesOwnerAvgSalesCycle();
    }
    @Override
    public List<SalesOwnerBantCompleteRateResponse> getSalesOwnerBantCompleteRate() {
        return leadRepo.getSalesOwnerBantCompleteRate();
    }
    @Override
    public List<SalesOwnerAvgBantScoreResponse> getSalesOwnerAvgBantScore() {
        return leadRepo.getSalesOwnerAvgBantScore();
    }
    @Override
    public List<LossReasonByProductLineResponse> getLossReasonByProductLine() {
        return leadRepo.getLossReasonByProductLine();
    }
    @Override
    public SalesOwnerDetailResponse getSalesOwnerDetail(String userCode) {
        return leadRepo.getSalesOwnerDetail(userCode);
    }

    @Override
    public List<com.helu.internship.entity.LeadSourceEntity> getAllLeadSources() {
        return leadSourceRepo.findAll();
    }
}

