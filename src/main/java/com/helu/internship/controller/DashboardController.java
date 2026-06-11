package com.helu.internship.controller;

import com.helu.internship.dto.response.*;
import com.helu.internship.service.DashboardService;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/win-rate-by-saleowner")
    public List<WinRateBySalesResponse> getWinRateBySalesOwner() {
        return dashboardService.getWinRateBySalesOwner();
    }

    @GetMapping("/cost-per-win-source")
    public List<CostPerWinBySourceResponse> getCostPerWinByLeadSource() {
        return dashboardService.getCostPerWinByLeadSource();
    }
    @GetMapping("/lost-reasons")
    public List<LostReasonSummaryProjection> getLostReasonSummary() {
        return dashboardService.getLostReasonSummary();
    }
    @GetMapping("/revenue-industry")
    public List<RevenueIndustryResponse> getRevenueByIndustry() {
        return dashboardService.getRevenueByIndustry();
    }
    @GetMapping("/roi-lead-source")
    public List<RoiLeadSourceResponse> getROIByLeadSource() {
        return dashboardService.getROIByLeadSource();
    }


}