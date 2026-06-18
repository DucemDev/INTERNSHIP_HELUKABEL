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

//Cung cấp dữ liệu cho Biểu đồ Phễu .
    @GetMapping("/lead-status")
    public List<LeadStatusCountResponse> getLeadStatusCount() {
        return dashboardService.getLeadStatusCount();
    }

//Lấy danh sách khách hàng chi tiết khi click vào các phần của biểu đồ.
    @GetMapping("/leads-by-status")
    public List<LeadByStatusResponse> getLeadsByStatus(@RequestParam String status) {
        return dashboardService.getLeadsByStatus(status);}

// API Cung cấp số liệu cho 3 thẻ KPI (Tổng Lead, Won Lead, Tỉ lệ chuyển đổi).
    @GetMapping("/conversion-rate")
    public ConversionRateResponse getConversionRate() {
        return dashboardService.getConversionRate();}

//Dữ liệu cho biểu đồ Tỉ lệ thắng theo nhân viên Sales. (win rate) [cần sửa]
    @GetMapping("/win-rate-by-saleowner")
    public List<WinRateBySalesResponse> getWinRateBySalesOwner() {
        return dashboardService.getWinRateBySalesOwner();}


    @GetMapping("/win-rate-by-industry")
    public List<WinRateByIndustryProjection> getWinRateByIndustry() {
        return dashboardService.getWinRateByIndustry();}


    @GetMapping("/win-rate-by-region")
    public List<WinRateByRegionProjection> getWinRateByRegion() {
        return dashboardService.getWinRateByRegion();}


//Dữ liệu cho biểu đồ Chi phí trên mỗi khách Won theo nguồn.
    @GetMapping("/cost-per-win-source")
    public List<CostPerWinBySourceResponse> getCostPerWinByLeadSource() {
        return dashboardService.getCostPerWinByLeadSource();}



    @GetMapping("/lost-reasons")
    public List<LostReasonSummaryProjection> getLostReasonSummary(
            @RequestParam(required = false) String productId
    ) {return dashboardService.getLostReasonSummary(productId);}


    @GetMapping("/revenue-industry")
    public List<RevenueIndustryResponse> getRevenueByIndustry() {
        return dashboardService.getRevenueByIndustry();}


    @GetMapping("/roi-lead-source")
    public List<RoiLeadSourceResponse> getROIByLeadSource() {
        return dashboardService.getROIByLeadSource();}



}