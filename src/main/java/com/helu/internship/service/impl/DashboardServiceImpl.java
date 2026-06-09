package com.helu.internship.service.impl;

import com.helu.internship.dto.response.LeadByStatusResponse;
import com.helu.internship.dto.response.LeadSourceCostProjection;
import com.helu.internship.dto.response.LeadStatusCountResponse;
import com.helu.internship.dto.response.PipelineCoverageProjection;
import com.helu.internship.repo.CostPerLeadRepo;
import com.helu.internship.repo.LeadRepo;
import com.helu.internship.repo.LeadStatusHistoryRepo;
import com.helu.internship.repo.PipelineCoveragerRepo;
import com.helu.internship.service.DashboardService;
import org.springframework.stereotype.Service;

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
    public List<PipelineCoverageProjection> getPipelineCoverage() {
        return pipelineCoverageRepo.getPipelineCoverage();
    }
}