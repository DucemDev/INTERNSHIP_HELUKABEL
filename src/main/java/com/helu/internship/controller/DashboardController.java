package com.helu.internship.controller;

import com.helu.internship.dto.response.ConversionRateResponse;
import com.helu.internship.dto.response.CostPerWinBySourceResponse;
import com.helu.internship.dto.response.WinRateBySalesResponse;
import com.helu.internship.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;
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
}
