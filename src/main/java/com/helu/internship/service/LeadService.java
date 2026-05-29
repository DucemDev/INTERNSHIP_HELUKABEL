package com.helu.internship.service;

import com.helu.internship.dto.request.LeadRequest;
import com.helu.internship.dto.response.LeadResponse;

import java.util.List;

public interface LeadService {
    List<LeadResponse> getAllLeads();
    LeadResponse getLeadById(String id);
    LeadResponse createLead(LeadRequest request);
    LeadResponse updateLead(String id, LeadRequest request);
    void deleteLead(String id);
}
