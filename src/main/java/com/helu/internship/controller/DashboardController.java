package com.helu.internship.controller;

import com.helu.internship.dto.response.*;
import com.helu.internship.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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

    @GetMapping("/revenue-industry")
    public List<RevenueIndustryResponse> getRevenueByIndustry() {
        return dashboardService.getRevenueByIndustry();
    }

    @GetMapping("/roi-lead-source")
    public List<RoiLeadSourceResponse> getROIByLeadSource() {
        return dashboardService.getROIByLeadSource();
    }

    @GetMapping("/pipeline-coverage")
    public List<PipelineCoverageProjection> getPipelineCoverage() {
        return dashboardService.getPipelineCoverage(null);
    }

    // --- STAFF ENDPOINTS ---

    @GetMapping("/staff/stats")
    public ConversionRateResponse getStaffStats(Principal principal) {
        return dashboardService.getStaffStats(principal.getName());
    }

    @GetMapping("/staff/pipeline-coverage")
    public List<PipelineCoverageProjection> getStaffPipelineCoverage(Principal principal) {
        return dashboardService.getStaffPipelineCoverage(principal.getName());
    }
}
