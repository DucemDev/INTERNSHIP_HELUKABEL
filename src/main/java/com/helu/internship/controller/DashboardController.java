package com.helu.internship.controller;

import com.helu.internship.dto.response.LeadByStatusResponse;
import com.helu.internship.dto.response.LeadStatusCountResponse;
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
}