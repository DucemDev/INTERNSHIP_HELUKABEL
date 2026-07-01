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

    @GetMapping("/lead-source-summary")
    public List<LeadSourceSummaryResponse> getLeadSourceSummary() {
        return dashboardService.getLeadSourceSummary();
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
    public List<PipelineCoverageProjection> getStaffPipelineCoverage(
            @RequestParam(value = "quarter", required = false) Integer quarter,
            @RequestParam(value = "year", required = false) Integer year,
            Principal principal) {
        return dashboardService.getStaffPipelineCoverage(principal.getName(), quarter, year);
    }

    @GetMapping("/seller/kpi-leads")
    public java.util.Map<String, Object> getStaffKpiLeads(
            @RequestParam(value = "quarter", required = false) Integer quarter,
            @RequestParam(value = "year", required = false) Integer year,
            Principal principal) {
        return dashboardService.getStaffKpiLeads(principal.getName(), quarter, year);
    }

    @GetMapping("/seller/leads-by-status-count")
    public List<LeadStatusCountResponse> getSellerLeadsByStatusCount(Principal principal) {
        return dashboardService.getSellerLeadsByStatusCount(principal.getName());
    }
    @GetMapping("/sales-owner-dashboard")
    public List<SalesOwnerDashboardProjection> getSalesOwnerDashboard() {
        return dashboardService.getSalesOwnerDashboard();
    }
    @GetMapping("/sales-owner-dashboard-by-quarter")
    public List<SalesOwnerDashboardProjection> getSalesOwnerDashboardByQuarter(@RequestParam String quarter, @RequestParam(required = false) Integer year) {
        return dashboardService.getSalesOwnerDashboardByQuarter(quarter, year);
    }
    @GetMapping("/win-rate-by-saleowner-by-quarter")
    public List<WinRateBySalesResponse> getWinRateBySalesOwnerByQuarter(@RequestParam String quarter, @RequestParam(required = false) Integer year) {
        return dashboardService.getWinRateBySalesOwnerByQuarter(quarter, year);
    }
    @GetMapping("/pipeline-coverage-by-quarter")
    public List<PipelineCoverageProjection> getPipelineCoverageByQuarter(@RequestParam String quarter, @RequestParam(required = false) Integer year) {
        return dashboardService.getPipelineCoverageByQuarter(quarter, year);
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
    public List<RevenueQuarterlyProjection> getRevenueQuarterly(@RequestParam(required = false) Integer year) {
        return dashboardService.getRevenueQuarterly(year);
    }

    @GetMapping("/lead-monthly")
    public List<LeadMonthlyProjection> getLeadMonthly() {
        return dashboardService.getLeadMonthly();
    }

    @GetMapping("/lead-quarterly")
    public List<LeadQuarterlyProjection> getLeadQuarterly(@RequestParam(required = false) Integer year) {
        return dashboardService.getLeadQuarterly(year);
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
    // Doanh thu theo khách hàng//
    @GetMapping("/revenue/account")
    public List<RevenueByAccountResponse> getRevenueByAccount() {
        return dashboardService.getRevenueByAccount();
    }
    // Dashboard khách hàng tìm năng theo nghành//
    @GetMapping("/potential-lead/industry")
    public List<LeadByIndustryResponse> getPotentialLeadByIndustry() {
        return dashboardService.getPotentialLeadByIndustry();
    }
// Phễu lead theo lead source//
    @GetMapping("/lead-funnel/source")
    public List<LeadFunnelBySourceResponse> getLeadFunnelBySource(
            @RequestParam(required = false) String sourceId
    ) {
        return dashboardService.getLeadFunnelBySource(sourceId);
    }
// dashboard lead source theo sản phẩm//
    @GetMapping("/lead-source-by-product")
    public List<LeadSourceByProductProjection> getLeadSourceByProduct() {
        return dashboardService.getLeadSourceByProduct();
    }
    // dashboard  doanh thu theoo lead source và sản phẩm//
    @GetMapping("/revenue-by-source-product")
    public List<RevenueBySourceProductResponse> getRevenueBySourceProduct() {
        return dashboardService.getRevenueBySourceProduct();
    }
    // dashboard  doanh thu theoo lead source và sản phẩm//
    @GetMapping("/won-lead-by-source-product")
    public List<WonLeadBySourceProductResponse> getWonLeadBySourceProduct() {
        return dashboardService.getWonLeadBySourceProduct();
    }

// dáshboard tong số lead lost và lý do theo source va san pham //
    @GetMapping("/lost-lead-by-source-product")
    public List<LostLeadBySourceResponse> getLostLeadBySource() {
        return dashboardService.getLostLeadBySource();
    }
    @GetMapping("/total-accounts")
    public Long countTotalAccounts() {
        return dashboardService.countTotalAccounts();
    }

    @GetMapping("/won-accounts")
    public Long countWonAccounts() {
        return dashboardService.countWonAccounts();
    }

    @GetMapping("/top-underserved-segment")
    public TopUnderservedSegmentProjection getTopUnderservedSegment() {
        return dashboardService.getTopUnderservedSegment();
    }

    // dashoard khách hàng có doanh thu cao nhất //
    @GetMapping("/best-account-revenue")
    public BestAccountRevenueResponse getBestAccountByRevenue() {
        return dashboardService.getBestAccountByRevenue();
    }
// dáshboard industry có tổng won cao nhất //
    @GetMapping("/best-industry-won-deal")
    public BestIndustryByWonDealResponse getBestIndustryByWonDeal() {
        return dashboardService.getBestIndustryByWonDeal();
    }
// dashboard industry có doanh thu cao nhất //
    @GetMapping("/best-industry-revenue")
    public BestIndustryByRevenueResponse getBestIndustryByRevenue() {
        return dashboardService.getBestIndustryByRevenue();
    }
    // dashboard vùng có nnhieeuff deal won //
    @GetMapping("/best-region-won-deal")
    public BestRegionByWonDealResponse getBestRegionByWonDeal() {
        return dashboardService.getBestRegionByWonDeal();
    }
// dashboard vùng có doanh thu cao nhất //
    @GetMapping("/best-region-revenue")
    public BestRegionByRevenueResponse getBestRegionByRevenue() {
        return dashboardService.getBestRegionByRevenue();
    }
// dashboard customer group có lead nhiều nhất //
    @GetMapping("/best-customer-group-lead")
    public BestCustomerGroupByLeadResponse getBestCustomerGroupByLead() {
        return dashboardService.getBestCustomerGroupByLead();
    }
// dashboard customer group có doanh thu nhiều nhất
    @GetMapping("/best-customer-group-revenue")
    public BestCustomerGroupByRevenueResponse getBestCustomerGroupByRevenue() {
        return dashboardService.getBestCustomerGroupByRevenue();
    }
// dashboard tổng lead theo industry //
    @GetMapping("/industry/total-leads")
    public TotalLeadByIndustryResponse getTotalLeadByIndustry(
            @RequestParam String industry
    ) {
        return dashboardService.getTotalLeadByIndustry(industry);
    }
// dashboard tổng lead won theo industry //
    @GetMapping("/industry/won-leads")
    public List<WonLeadByIndustryResponse> getWonLeadByIndustry() {
        return dashboardService.getWonLeadByIndustry();
    }
// dashboard conversion rate theo industry //
    @GetMapping("/industry/conversion-rate")
    public List<IndustryConversionRateResponse> getIndustryConversionRate() {
        return dashboardService.getIndustryConversionRate();
    }
// dashboard trung bình vòng đời chốt đơn //
    @GetMapping("/industry/avg-sales-cycle")
    public List<AvgSalesCycleByIndustryResponse> getAvgSalesCycleByIndustry() {
        return dashboardService.getAvgSalesCycleByIndustry();
    }
// dashboard lý do nhều nhất theo industry //
    @GetMapping("/industry/best-lost-reason")
    public List<BestLostReasonByIndustryResponse> getBestLostReasonByIndustry() {
        return dashboardService.getBestLostReasonByIndustry();
    }
// dashboard doanh thu theo customer-role //
    @GetMapping("/customer-role/revenue")
    public List<RevenueByCustomerRoleResponse> getRevenueByCustomerRole() {
        return dashboardService.getRevenueByCustomerRole();
    }
// dashboard tổng lead theo customer-role
    @GetMapping("/customer-role/total-leads")
    public List<TotalLeadByCustomerRoleResponse> getTotalLeadByCustomerRole() {
        return dashboardService.getTotalLeadByCustomerRole();
    }
// dashboard conversion-rate theo customer-role //
@GetMapping("/customer-role/conversion-rate")
public List<CustomerRoleConversionRateResponse> getCustomerRoleConversionRate() {
    return dashboardService.getCustomerRoleConversionRate();
}
// dashboard vòng đời trung bình chốt deal theo customer //
    @GetMapping("/customer-role/avg-revenue-won")
    public List<AvgRevenueWonByCustomerRoleResponse> getAvgRevenueWonByCustomerRole() {
        return dashboardService.getAvgRevenueWonByCustomerRole();
    }
// dashboard tổng lead lost theo customer-role //
    @GetMapping("/customer-role/lost-leads")
    public List<LostLeadByCustomerRoleResponse> getLostLeadByCustomerRole() {
        return dashboardService.getLostLeadByCustomerRole();
    }
// dashboard lý do thua nhiều nhất theo customer-role //
    @GetMapping("/customer-role/best-lost-reason")
    public List<BestLostReasonByCustomerRoleResponse> getBestLostReasonByCustomerRole() {
        return dashboardService.getBestLostReasonByCustomerRole();
    }
// dashboard số lead theo vùng //
    @GetMapping("/region/total-leads")
    public List<TotalLeadByRegionResponse> getTotalLeadByRegion() {
        return dashboardService.getTotalLeadByRegion();
    }
// dashboard số lead won theo vùng //
    @GetMapping("/region/won-leads")
    public List<WonLeadByRegionResponse> getWonLeadByRegion() {
        return dashboardService.getWonLeadByRegion();
    }
// dashboard conversion-rate  theo vùng //
    @GetMapping("/region/conversion-rate")
    public List<RegionConversionRateResponse> getRegionConversionRate() {
        return dashboardService.getRegionConversionRate();
    }
// dashboard vòng đời trung bình chốt deal theo vùng //
    @GetMapping("/region/avg-revenue-won")
    public List<AvgRevenueWonByRegionResponse> getAvgRevenueWonByRegion() {
        return dashboardService.getAvgRevenueWonByRegion();
    }
// dashnoard số lead lost theo vùng //
    @GetMapping("/region/lost-leads")
    public List<LostLeadByRegionResponse> getLostLeadByRegion() {
        return dashboardService.getLostLeadByRegion();
    }
// dashboard lý do lost nhiều nhâất //
    @GetMapping("/region/best-lost-reason")
    public List<BestLostReasonByRegionResponse> getBestLostReasonByRegion() {
        return dashboardService.getBestLostReasonByRegion();
    }
// dashboard tổng lead theo sản phẩm //
    @GetMapping("/product-line/total-leads")
    public List<TotalLeadByProductLineResponse> getTotalLeadByProductLine() {
        return dashboardService.getTotalLeadByProductLine();
    }
// dashboard số won lead theo sản phẩm //
    @GetMapping("/product-line/won-leads")
    public List<WonLeadByProductLineResponse> getWonLeadByProductLine() {
        return dashboardService.getWonLeadByProductLine();
    }
// dashboard conversion-rate theo sản phẩm
    @GetMapping("/product-line/conversion-rate")
    public List<ProductLineConversionRateResponse> getProductLineConversionRate() {
        return dashboardService.getProductLineConversionRate();
    }
// dashboard vòng đời trung bình chốt deal theo sản phẩm
    @GetMapping("/product-line/avg-revenue-won")
    public List<AvgRevenueWonByProductLineResponse> getAvgRevenueWonByProductLine() {
        return dashboardService.getAvgRevenueWonByProductLine();
    }
// dashboard sô lead lost theo sản phẩm
    @GetMapping("/product-line/lost-leads")
    public List<LostLeadByProductLineResponse> getLostLeadByProductLine() {
        return dashboardService.getLostLeadByProductLine();
    }
// dashboard lý do thua nhiều nhất theo sản phẩm
    @GetMapping("/product-line/best-lost-reason")
    public List<BestLostReasonByProductLineResponse> getBestLostReasonByProductLine() {
        return dashboardService.getBestLostReasonByProductLine();
    }
// dashboard tiính roi theo customer-group //
    @GetMapping("/customer-group/roi")
    public List<CustomerGroupROIResponse> getCustomerGroupROI() {
        return dashboardService.getCustomerGroupROI();
    }
// dashboard cost-per-lead theo customer-group //
    @GetMapping("/customer-group/cost-per-lead")
    public List<CustomerGroupCPLResponse> getCustomerGroupCostPerLead() {
        return dashboardService.getCustomerGroupCostPerLead();
    }
// dashboard top 10 accounts lấy chi tiết cho bảng table //
    @GetMapping("/top10-accounts")
    public List<Top10AccountRevenueResponse> getTop10Accounts() {
        return dashboardService.getTop10Accounts();
    }
// dashboard nhân viên có doanh thu cao nhất //
    @GetMapping("/top-sales-owner-revenue")
    public TopSalesOwnerRevenueResponse getTopSalesOwnerRevenue() {
        return dashboardService.getTopSalesOwnerRevenue();
    }
// dashboard nhân viên có có win rate
    @GetMapping("/top-sales-owner-win-rate")
    public TopSalesOwnerWinRateResponse getTopSalesOwnerWinRate() {
        return dashboardService.getTopSalesOwnerWinRate();
    }
    // dashboard nhân viên có thời gian chốt deal nhanh nhất //
    @GetMapping("/fastest-sales-owner")
    public FastestSalesOwnerResponse getFastestSalesOwner() {
        return dashboardService.getFastestSalesOwner();
    }
// dashboard nhân viên có trung bình thời gian chốt deal
    @GetMapping("/sales-owner/avg-sales-cycle")
    public List<SalesOwnerAvgSalesCycleResponse> getSalesOwnerAvgSalesCycle() {
        return dashboardService.getSalesOwnerAvgSalesCycle();
    }
// dashboard % điểm bant theo sale
    @GetMapping("/sales-owner/bant-complete-rate")
    public List<SalesOwnerBantCompleteRateResponse> getSalesOwnerBantCompleteRate() {
        return dashboardService.getSalesOwnerBantCompleteRate();
    }
// dashboard trung bình điểm bant theo sale //
    @GetMapping("/sales-owner/avg-bant-score")
    public List<SalesOwnerAvgBantScoreResponse> getSalesOwnerAvgBantScore() {
        return dashboardService.getSalesOwnerAvgBantScore();
    }

    // Customer Value Matrix chart data
    //biểu đồ bà lôn
    @GetMapping("/customer-value-matrix")
    public List<CustomerValueMatrixResponse> getCustomerValueMatrix() {
        return dashboardService.getCustomerValueMatrix();
    }

    // dashboard các lý do thua theo sản phẩm //
    @GetMapping("/product-line/loss-reasons")
    public List<LossReasonByProductLineResponse> getLossReasonByProductLine() {
        return dashboardService.getLossReasonByProductLine();
    }
// dashboard sale bảrng chi tiết (- Qualified Leads
//- Won Leads
//- Lost Leads
//- Win Rate
//- Avg Deal Size
//- Avg Sales Cycle )
    @GetMapping("/sales-owner/detail")
    public SalesOwnerDetailResponse getSalesOwnerDetail(
            @RequestParam String userCode
    ) {
        return dashboardService.getSalesOwnerDetail(userCode);
    }
    // trung bình tiền chi cho mỗi lead
    @GetMapping("/source/avg-cost-per-lead")
    public List<AvgCostPerLeadBySourceResponse> getAvgCostPerLeadBySource() {
        return dashboardService.getAvgCostPerLeadBySource();
    }


    @GetMapping("/daily-compare")
    public DailyCompareResponse getDailyCompare() {
        return dashboardService.getDailyCompare();
    }






























}
