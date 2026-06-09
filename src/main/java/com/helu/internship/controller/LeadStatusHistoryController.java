package com.helu.internship.controller;


import com.helu.internship.dto.response.LeadSourceCostProjection;
import com.helu.internship.dto.response.PipelineCoverageProjection;
import com.helu.internship.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
    @RequestMapping("/api/dashboard")
    public class LeadStatusHistoryController {
    private final DashboardService dashboardService;

    public LeadStatusHistoryController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/average-days-to-won")
    public Double getAverageDaysToWon() {
        return dashboardService.getAverageDaysToWon();
    }

    @GetMapping("/lead-source-cost")
    public List<LeadSourceCostProjection> getLeadSourceCostDashboard() {
        return dashboardService.getLeadSourceCostDashboard();
    }
    @GetMapping("/pipeline-coverage")
    public List<PipelineCoverageProjection> getPipelineCoverage() {
        return dashboardService.getPipelineCoverage();
    }
}
