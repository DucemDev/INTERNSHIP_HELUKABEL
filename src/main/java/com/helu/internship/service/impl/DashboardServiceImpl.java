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
    public DashboardServiceImpl(
            LeadRepo leadRepo,
            LeadStatusHistoryRepo leadStatusHistoryRepo,
            CostPerLeadRepo costPerLeadRepo,
            PipelineCoveragerRepo pipelineCoverageRepo,
            UserRepo userRepo,
            SalesOwnerDashboardRepo salesOwnerDashboardRepo) {

        this.leadRepo = leadRepo;
        this.leadStatusHistoryRepo = leadStatusHistoryRepo;
        this.costPerLeadRepo = costPerLeadRepo;
        this.pipelineCoverageRepo = pipelineCoverageRepo;
        this.userRepo = userRepo;
        this.salesOwnerDashboardRepo = salesOwnerDashboardRepo;
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
    public ConversionRateResponse getStaffStats(String email) {
        return leadRepo.getStatsByEmail(email);
    }

    @Override
    public List<PipelineCoverageProjection> getStaffPipelineCoverage(String email) {
        String userCode = userRepo.findByEmail(email)
                .map(u -> u.getUserCode())
                .orElse(null);
        return pipelineCoverageRepo.getPipelineCoverage(userCode);
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
}
