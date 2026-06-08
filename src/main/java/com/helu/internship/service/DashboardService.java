package com.helu.internship.service;

import com.helu.internship.dto.response.ConversionRateResponse;
import com.helu.internship.dto.response.CostPerWinBySourceResponse;
import com.helu.internship.dto.response.WinRateBySalesResponse;

import java.util.List;

public interface DashboardService {
    ConversionRateResponse getConversionRate();
    List<WinRateBySalesResponse> getWinRateBySalesOwner();
    List<CostPerWinBySourceResponse> getCostPerWinByLeadSource();
}
