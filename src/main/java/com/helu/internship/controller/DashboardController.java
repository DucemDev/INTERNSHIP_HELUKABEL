package com.helu.internship.controller;

import com.helu.internship.dto.response.*;
import com.helu.internship.service.DashboardService;
import org.springframework.web.bind.annotation.*;


import java.security.Principal;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/lead-status")
    public List<LeadStatusCountResponse> getLeadStatusCount() {
        return dashboardService.getLeadStatusCount();
    }

    @GetMapping("/leads-by-status")
    public List<LeadByStatusResponse> getLeadsByStatus(@RequestParam String status) {
        return dashboardService.getLeadsByStatus(status);
    }

    @GetMapping("/conversion-rate")
    public ConversionRateResponse getConversionRate() {
        return dashboardService.getConversionRate();
    }

    @GetMapping("/average-days-to-won")
    public Double getAverageDaysToWon() {
        return dashboardService.getAverageDaysToWon();
    }

    @GetMapping("/win-rate-by-saleowner")
    public List<WinRateBySalesResponse> getWinRateBySalesOwner(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String industry
    ) {
        return dashboardService.getWinRateBySalesOwner(region, industry);
    }

    @GetMapping("/win-rate-by-industry")
    public List<WinRateByIndustryProjection> getWinRateByIndustry() {
        return dashboardService.getWinRateByIndustry();
    }

    @GetMapping("/win-rate-by-region")
    public List<WinRateByRegionProjection> getWinRateByRegion() {
        return dashboardService.getWinRateByRegion();
    }

    @GetMapping("/cost-per-win-source")
    public List<CostPerWinBySourceResponse> getCostPerWinByLeadSource() {
        return dashboardService.getCostPerWinByLeadSource();
    }

    @GetMapping("/lead-source-cost")
    public List<LeadSourceCostProjection> getLeadSourceCostDashboard() {
        return dashboardService.getLeadSourceCostDashboard();
    }

    @GetMapping("/lost-reasons")
    public List<LostReasonSummaryProjection> getLostReasonSummary(
            @RequestParam(required = false) String productId
    ) {
        return dashboardService.getLostReasonSummary(productId);
    }
    @GetMapping("/lost-by-seller")
    public List<LostBySellerProjection> getLostBySeller() {
        return dashboardService.getLostBySeller();
    }
    @GetMapping("/lost-by-source")
    public List<LostBySourceProjection> getLostBySource() {
        return dashboardService.getLostBySource();
    }
    @GetMapping("/lost-by-region")
    public List<LostByRegionProjection> getLostByRegion() {
        return dashboardService.getLostByRegion();
    }
    @GetMapping("/lost-by-industry")
    public List<LostByIndustryProjection> getLostByIndustry() {
        return dashboardService.getLostByIndustry();
    }
    @GetMapping("/revenue-industry")
    public List<RevenueIndustryResponse> getRevenueByIndustry() {
        return dashboardService.getRevenueByIndustry();
    }

    @GetMapping("/roi-lead-source")
    public List<RoiLeadSourceResponse> getROIByLeadSource() {
        return dashboardService.getROIByLeadSource();
    }
    @GetMapping("/conversion-rate/filter")
    public List<ConversionRateResponse> getConversionRateFilter(
            @RequestParam(required = false) String sourceId,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String salesOwnerId,
            @RequestParam(required = false) String customerGroup,
            @RequestParam(required = false) LocalDate timeFrom,
            @RequestParam(required = false) LocalDate timeTo
    ) {
        return dashboardService.getConversionRateFilter(
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

    @GetMapping("/pipeline-coverage")
    public List<PipelineCoverageProjection> getPipelineCoverage() {
        return dashboardService.getPipelineCoverage(null);
    }

    // --- SELLER ENDPOINTS ---

    @GetMapping("/seller/stats")
    public ConversionRateResponse getStaffStats(Principal principal) {
        return dashboardService.getStaffStats(principal.getName());
    }

    @GetMapping("/seller/pipeline-coverage")
    public List<PipelineCoverageProjection> getStaffPipelineCoverage(Principal principal) {
        return dashboardService.getStaffPipelineCoverage(principal.getName());
    }
    @GetMapping("/sales-owner-dashboard")
    public List<SalesOwnerDashboardProjection> getSalesOwnerDashboard() {
        return dashboardService.getSalesOwnerDashboard();
    }
    @GetMapping("/revenue-summary")
    public RevenueSummaryProjection getRevenueSummary() {
        return dashboardService.getRevenueSummary();
    }

    @GetMapping("/revenue-region")
    public List<RevenueRegionProjection> getRevenueByRegion() {
        return dashboardService.getRevenueByRegion();
    }

    @GetMapping("/revenue-product-line")
    public List<RevenueProductLineProjection> getRevenueByProductLine() {
        return dashboardService.getRevenueByProductLine();
    }
    @GetMapping("/revenue-monthly")
    public List<RevenueMonthlyProjection> getRevenueMonthly() {
        return dashboardService.getRevenueMonthly();
    }

    @GetMapping("/revenue-quarterly")
    public List<RevenueQuarterlyProjection> getRevenueQuarterly() {
        return dashboardService.getRevenueQuarterly();
    }

    @GetMapping("/lead-monthly")
    public List<LeadMonthlyProjection> getLeadMonthly() {
        return dashboardService.getLeadMonthly();
    }

    @GetMapping("/lead-quarterly")
    public List<LeadQuarterlyProjection> getLeadQuarterly() {
        return dashboardService.getLeadQuarterly();
    }
    @GetMapping("/revenue-seller-monthly")
    public List<RevenueSellerMonthlyProjection> getRevenueSellerMonthly() {
        return dashboardService.getRevenueSellerMonthly();
    }

    @GetMapping("/revenue-source-monthly")
    public List<RevenueSourceMonthlyProjection> getRevenueSourceMonthly() {
        return dashboardService.getRevenueSourceMonthly();
    }

    @GetMapping("/revenue-region-monthly")
    public List<RevenueRegionMonthlyProjection> getRevenueRegionMonthly() {
        return dashboardService.getRevenueRegionMonthly();
    }

    @GetMapping("/revenue-industry-monthly")
    public List<RevenueIndustryMonthlyProjection> getRevenueIndustryMonthly() {
        return dashboardService.getRevenueIndustryMonthly();
    }

    @GetMapping("/revenue-product-line-monthly")
    public List<RevenueProductLineMonthlyProjection> getRevenueProductLineMonthly() {
        return dashboardService.getRevenueProductLineMonthly();
    }
}
