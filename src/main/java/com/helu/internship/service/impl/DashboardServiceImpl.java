package com.helu.internship.service.impl;

import com.helu.internship.dto.response.ConversionRateResponse;
import com.helu.internship.dto.response.CostPerWinBySourceResponse;
import com.helu.internship.dto.response.WinRateBySalesResponse;
import com.helu.internship.repo.LeadRepo;
import com.helu.internship.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class DashboardServiceImpl implements DashboardService {
    private final LeadRepo leadRepo;
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
}
