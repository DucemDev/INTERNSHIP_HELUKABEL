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
    java.util.Map<String, Object> getLeadMetadata();
    String getNextLeadId();

    List<LeadResponse> getSellerLeads(String email, String heatFilter);
    LeadResponse updateSellerLeadStatus(String email, String leadId, String status);
    LeadResponse updateSellerLeadBant(String email, String leadId, int budget, int authority, int need, int timeline);
}
