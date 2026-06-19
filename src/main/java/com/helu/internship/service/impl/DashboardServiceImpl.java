package com.helu.internship.service.impl;


import com.helu.internship.dto.response.*;

import com.helu.internship.repo.CostPerLeadRepo;
import com.helu.internship.repo.LeadRepo;
import com.helu.internship.repo.LeadStatusHistoryRepo;
import com.helu.internship.repo.PipelineCoveragerRepo;
import com.helu.internship.service.DashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final LeadRepo leadRepo;
    private final LeadStatusHistoryRepo leadStatusHistoryRepo;
    private final CostPerLeadRepo costPerLeadRepo;
    private final PipelineCoveragerRepo pipelineCoverageRepo;

    public DashboardServiceImpl(
            LeadRepo leadRepo,
            LeadStatusHistoryRepo leadStatusHistoryRepo,
            CostPerLeadRepo costPerLeadRepo,
            PipelineCoveragerRepo pipelineCoverageRepo) {

        this.leadRepo = leadRepo;
        this.leadStatusHistoryRepo = leadStatusHistoryRepo;
        this.costPerLeadRepo = costPerLeadRepo;
        this.pipelineCoverageRepo = pipelineCoverageRepo;
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
    public List<PipelineCoverageProjection> getPipelineCoverage(
            String sellerCode
    ) {return pipelineCoverageRepo.getPipelineCoverage(sellerCode);}

    @Override
    public ConversionRateResponse getConversionRate() {
        return leadRepo.getConversionRate();
    }

    @Override
    public List<WinRateBySalesResponse> getWinRateBySalesOwner() {
        return leadRepo.getWinRateBySalesOwner();
    }

    @Override
    public List<CostPerWinBySourceResponse> getCostPerWinByLeadSource() {
        return leadRepo.getCostPerWinByLeadSource();
    }

    @Override
    public List<LostReasonSummaryProjection> getLostReasonSummary() {
        return leadRepo.getLostReasonSummary(null);
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
    public List<ConversionRateResponse> getConversionRateFilter(
            String sourceId,
            String sourceType,
            String region,
            String industry,
            String salesOwnerId,
            String  customerGroup,
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
    public UnderServedSummaryResponse getUnderServedSummary() {
        List<UnderServedSegmentProjection> rawProjections = leadRepo.getRawCustomerConcentration();
        
        final BigDecimal totalWonRevenue = rawProjections.stream()
                .map(p -> p.getTotalRevenue() != null ? p.getTotalRevenue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<UnderServedSegmentResponse> customers = rawProjections.stream()
                .map(seg -> {
                    long totalLeads = seg.getTotalLeads();
                    long wonLeads = seg.getWonLeads();
                    BigDecimal totalRevenue = seg.getTotalRevenue() != null ? seg.getTotalRevenue() : BigDecimal.ZERO;

                    BigDecimal winRate = totalLeads == 0 ? BigDecimal.ZERO :
                            BigDecimal.valueOf(wonLeads)
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(BigDecimal.valueOf(totalLeads), 2, RoundingMode.HALF_UP);

                    BigDecimal revenuePerLead = totalLeads == 0 ? BigDecimal.ZERO :
                            totalRevenue.divide(BigDecimal.valueOf(totalLeads), 2, RoundingMode.HALF_UP);

                    // Under customer concentration, "opportunityScore" holds the contributionPercentage
                    BigDecimal contributionPercentage = totalWonRevenue.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
                            totalRevenue.multiply(BigDecimal.valueOf(100))
                                    .divide(totalWonRevenue, 2, RoundingMode.HALF_UP);

                    return UnderServedSegmentResponse.builder()
                            .segmentName(seg.getSegmentName())
                            .totalLeads(totalLeads)
                            .wonLeads(wonLeads)
                            .lostLeads(seg.getLostLeads())
                            .winRate(winRate)
                            .totalRevenue(totalRevenue)
                            .revenuePerLead(revenuePerLead)
                            .opportunityScore(contributionPercentage)
                            .build();
                })
                .sorted((c1, c2) -> c2.getTotalRevenue().compareTo(c1.getTotalRevenue()))
                .toList();

        String topCustomerName = customers.isEmpty() ? "N/A" : customers.get(0).getSegmentName();
        BigDecimal topCustomerShare = customers.isEmpty() ? BigDecimal.ZERO : customers.get(0).getOpportunityScore();
        BigDecimal top3ConcentrationRatio = customers.stream()
                .limit(3)
                .map(UnderServedSegmentResponse::getOpportunityScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return UnderServedSummaryResponse.builder()
                .customers(customers)
                .totalWonRevenue(totalWonRevenue)
                .topCustomerName(topCustomerName)
                .topCustomerShare(topCustomerShare)
                .top3ConcentrationRatio(top3ConcentrationRatio)
                .build();
    }
}

