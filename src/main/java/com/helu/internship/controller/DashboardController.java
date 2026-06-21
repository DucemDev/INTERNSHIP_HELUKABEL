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

/*/
Tổng số lead trong các giai đoạn
 */

    @GetMapping("/lead-status")
    public List<LeadStatusCountResponse> getLeadStatusCount() {
        return dashboardService.getLeadStatusCount();
    }
/*/
http://localhost:8080/api/dashboard/leads-by-status?status=new --- lấy danh sách các lead theo trạng thái (giai đoạn)
*/

    @GetMapping("/leads-by-status")
    public List<LeadByStatusResponse> getLeadsByStatus(@RequestParam String status) {
        return dashboardService.getLeadsByStatus(status);
    }

/*/
Tỉ lệ chuyển đổi
 */

    @GetMapping("/conversion-rate")
    public ConversionRateResponse getConversionRate() {
        return dashboardService.getConversionRate();
    }

/*/
Trung bình thời gian won 1 lead
 */

    @GetMapping("/average-days-to-won")
    public Double getAverageDaysToWon() {
        return dashboardService.getAverageDaysToWon();
    }

/*/
Tỉ lệ won 1 lead theo seller
 */

    @GetMapping("/win-rate-by-saleowner")
    public List<WinRateBySalesResponse> getWinRateBySalesOwner(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String industry
    ) {
        return dashboardService.getWinRateBySalesOwner(region, industry);
    }

/*/
Tỉ lệ won 1 lead theo lĩnh vực/ ngành công nghiệp
*/

    @GetMapping("/win-rate-by-industry")
    public List<WinRateByIndustryProjection> getWinRateByIndustry() {
        return dashboardService.getWinRateByIndustry();
    }

/*/
Tỉ lệ won 1 lead theo vùng (Bắc Trung Nam)
*/


    @GetMapping("/win-rate-by-region")
    public List<WinRateByRegionProjection> getWinRateByRegion() {
        return dashboardService.getWinRateByRegion();
    }

/*/
Tổng tiền chi ra cho từng nguồn vs tiền chi ra cho mỗi deal won từ nguồn đó
*/

    @GetMapping("/cost-per-win-source")
    public List<CostPerWinBySourceResponse> getCostPerWinByLeadSource() {
        return dashboardService.getCostPerWinByLeadSource();
    }

/*/
Tổng tiền chi ra cho mỗi nguồn
*/

    @GetMapping("/lead-source-cost")
    public List<LeadSourceCostProjection> getLeadSourceCostDashboard() {
        return dashboardService.getLeadSourceCostDashboard();
    }
/*/
Tổng các nguyên nhân lỗi và số lượng deal lost do nguyên nhân đó
*/

    @GetMapping("/lost-reasons")
    public List<LostReasonSummaryProjection> getLostReasonSummary(
            @RequestParam(required = false) String productId
    ) {
        return dashboardService.getLostReasonSummary(productId);
    }

/*/
Tổng các deal lost theo seller
*/

    @GetMapping("/lost-by-seller")
    public List<LostBySellerProjection> getLostBySeller() {
        return dashboardService.getLostBySeller();
    }

/*/
Tổng các deal lost theo từng source
*/

    @GetMapping("/lost-by-source")
    public List<LostBySourceProjection> getLostBySource() {
        return dashboardService.getLostBySource();
    }

/*/
Tổng các deal lost theo từng vùng
*/

    @GetMapping("/lost-by-region")
    public List<LostByRegionProjection> getLostByRegion() {
        return dashboardService.getLostByRegion();
    }

/*/
Tổng các deal lost theo từng lĩnh vực/ ngành công nghiệp
*/

    @GetMapping("/lost-by-industry")
    public List<LostByIndustryProjection> getLostByIndustry() {
        return dashboardService.getLostByIndustry();
    }

/*/
Tổng doanh thu theo từng industry -- http://localhost:8080/api/dashboard/revenue-industry?=3
*/

    @GetMapping("/revenue-industry")
    public List<RevenueIndustryResponse> getRevenueByIndustry() {
        return dashboardService.getRevenueByIndustry();
    }

/*/
Tổng ROI thu về từ từng nguồn
*/

    @GetMapping("/roi-lead-source")
    public List<RoiLeadSourceResponse> getROIByLeadSource() {
        return dashboardService.getROIByLeadSource();
    }

/*/
bộ lọc tìm kiếm tỉ lệ chuyển đổi theo source, sourceType, region, industry, salesOwnerId, customerGroup, timeFrom, timeTo
*/

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

/*/
Mức độ an toàn của target so với doanh thu dự kiến từ các lead đang nuôi dưỡng
*/

    @GetMapping("/pipeline-coverage")
    public List<PipelineCoverageProjection> getPipelineCoverage() {
        return dashboardService.getPipelineCoverage(null);
    }

    // --- STAFF ENDPOINTS ---

//    @GetMapping("/staff/stats")
//    public ConversionRateResponse getStaffStats(Principal principal) {
//        return dashboardService.getStaffStats(principal.getName());
//    }
//
//    @GetMapping("/staff/pipeline-coverage")
//    public List<PipelineCoverageProjection> getStaffPipelineCoverage(Principal principal) {
//        return dashboardService.getStaffPipelineCoverage(principal.getName());
//    }

/*/
Dashboard - power bi của role seller gồm wonLead, totalLead, totalRevenue, winRate, avgDaysToWon, openLead
*/

    @GetMapping("/sales-owner-dashboard")
    public List<SalesOwnerDashboardProjection> getSalesOwnerDashboard() {
        return dashboardService.getSalesOwnerDashboard();
    }

/*/
Tổng doanh thu và tổng leadwon của toàn bộ 150 lead
*/

    @GetMapping("/revenue-summary")
    public RevenueSummaryProjection getRevenueSummary() {
        return dashboardService.getRevenueSummary();
    }

/*/
Tổng doanh thu theo từng vùng/ khu vực
*/

    @GetMapping("/revenue-region")
    public List<RevenueRegionProjection> getRevenueByRegion() {
        return dashboardService.getRevenueByRegion();
    }

/*/
Tổng doanh thu theo từng sản phẩm
*/

    @GetMapping("/revenue-product-line")
    public List<RevenueProductLineProjection> getRevenueByProductLine() {
        return dashboardService.getRevenueByProductLine();
    }

/*/
Tổng doanh thu theo từng tháng
*/


    @GetMapping("/revenue-monthly")
    public List<RevenueMonthlyProjection> getRevenueMonthly() {
        return dashboardService.getRevenueMonthly();
    }

/*/
Tổng doanh thu theo từng quý
*/

    @GetMapping("/revenue-quarterly")
    public List<RevenueQuarterlyProjection> getRevenueQuarterly() {
        return dashboardService.getRevenueQuarterly();
    }

/*/
Tổng số lead theo từng tháng
*/
    @GetMapping("/lead-monthly")
    public List<LeadMonthlyProjection> getLeadMonthly() {
        return dashboardService.getLeadMonthly();
    }

/*/
Tổng số lead theo từng quý
*/

    @GetMapping("/lead-quarterly")
    public List<LeadQuarterlyProjection> getLeadQuarterly() {
        return dashboardService.getLeadQuarterly();
    }

/*/
Tổng doanh thu của từng seller theo tháng
*/
    @GetMapping("/revenue-seller-monthly")
    public List<RevenueSellerMonthlyProjection> getRevenueSellerMonthly() {
        return dashboardService.getRevenueSellerMonthly();
    }

/*/
Tổng doanh thu của từng nguồn theo tháng
*/
    @GetMapping("/revenue-source-monthly")
    public List<RevenueSourceMonthlyProjection> getRevenueSourceMonthly() {
        return dashboardService.getRevenueSourceMonthly();
    }

/*/
Tổng doanh thu của từng vùng/khu vực theo tháng
*/

    @GetMapping("/revenue-region-monthly")
    public List<RevenueRegionMonthlyProjection> getRevenueRegionMonthly() {
        return dashboardService.getRevenueRegionMonthly();
    }

/*/
Tổng doanh thu của từng lĩnh vực/ngành công nghiệp theo tháng
*/

    @GetMapping("/revenue-industry-monthly")
    public List<RevenueIndustryMonthlyProjection> getRevenueIndustryMonthly() {
        return dashboardService.getRevenueIndustryMonthly();
    }

/*/
Tổng doanh thu của từng sản phẩm theo tháng
*/

    @GetMapping("/revenue-product-line-monthly")
    public List<RevenueProductLineMonthlyProjection> getRevenueProductLineMonthly() {
        return dashboardService.getRevenueProductLineMonthly();
    }
}
