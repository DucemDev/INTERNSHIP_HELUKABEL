package com.helu.internship.service;

import com.helu.internship.dto.response.LeadByStatusResponse;
import com.helu.internship.dto.response.LeadStatusCountResponse;

import java.util.List;

public interface DashboardService {
    List<LeadStatusCountResponse> getLeadStatusCount();
    List<LeadByStatusResponse> getLeadsByStatus(String status);
}
