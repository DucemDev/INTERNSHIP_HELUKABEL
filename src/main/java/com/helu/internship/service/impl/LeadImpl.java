package com.helu.internship.service.impl;

import com.helu.internship.dto.request.LeadRequest;
import com.helu.internship.dto.response.LeadResponse;
import com.helu.internship.entity.*;
import com.helu.internship.repo.*;
import com.helu.internship.service.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadImpl implements LeadService {

    private final LeadRepo leadRepo;
    private final ProductRepo productRepo;
    private final LeadSourceRepo leadSourceRepo;
    private final UserRepo userRepo;

    @Override
    public List<LeadResponse> getAllLeads() {
        return leadRepo.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LeadResponse getLeadById(String id) {
        LeadEntity lead = leadRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        return mapToResponse(lead);
    }

    @Override
    @Transactional
    public LeadResponse createLead(LeadRequest request) {
        ProductEntity product = productRepo.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        LeadSourceEntity source = leadSourceRepo.findById(request.getSourceId())
                .orElseThrow(() -> new RuntimeException("Source not found"));
        UserEntity user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeadEntity lead = LeadEntity.builder()
                .leadId(request.getLeadId())
                .createdDate(request.getCreatedDate())
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .account(request.getAccount())
                .industryType(request.getIndustryType())
                .customerGroup(request.getCustomerGroup())
                .customerRole(request.getCustomerRole())
                .location(request.getLocation())
                .region(request.getRegion())
                .status(request.getStatus())
                .cost(request.getCost())
                .lossReason(request.getLossReason())
                .businessResult(request.getBusinessResult())
                .product(product)
                .source(source)
                .user(user)
                .build();

        return mapToResponse(leadRepo.save(lead));
    }

    @Override
    @Transactional
    public LeadResponse updateLead(String id, LeadRequest request) {
        LeadEntity lead = leadRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        if (request.getProductId() != null) {
            ProductEntity product = productRepo.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            lead.setProduct(product);
        }
        if (request.getSourceId() != null) {
            LeadSourceEntity source = leadSourceRepo.findById(request.getSourceId())
                    .orElseThrow(() -> new RuntimeException("Source not found"));
            lead.setSource(source);
        }
        if (request.getUserId() != null) {
            UserEntity user = userRepo.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            lead.setUser(user);
        }

        lead.setFullName(request.getFullName());
        lead.setPhoneNumber(request.getPhoneNumber());
        lead.setEmail(request.getEmail());
        lead.setAccount(request.getAccount());
        lead.setIndustryType(request.getIndustryType());
        lead.setCustomerGroup(request.getCustomerGroup());
        lead.setCustomerRole(request.getCustomerRole());
        lead.setLocation(request.getLocation());
        lead.setRegion(request.getRegion());
        lead.setStatus(request.getStatus());
        lead.setCost(request.getCost());
        lead.setLossReason(request.getLossReason());
        lead.setBusinessResult(request.getBusinessResult());

        return mapToResponse(leadRepo.save(lead));
    }

    @Override
    @Transactional
    public void deleteLead(String id) {
        leadRepo.deleteById(id);
    }

    private LeadResponse mapToResponse(LeadEntity lead) {
        return LeadResponse.builder()
                .leadId(lead.getLeadId())
                .createdDate(lead.getCreatedDate())
                .fullName(lead.getFullName())
                .phoneNumber(lead.getPhoneNumber())
                .email(lead.getEmail())
                .account(lead.getAccount())
                .industryType(lead.getIndustryType())
                .customerGroup(lead.getCustomerGroup())
                .customerRole(lead.getCustomerRole())
                .location(lead.getLocation())
                .region(lead.getRegion())
                .status(lead.getStatus())
                .cost(lead.getCost())
                .lossReason(lead.getLossReason())
                .businessResult(lead.getBusinessResult())
                .productName(lead.getProduct().getProductName())
                .sourceName(lead.getSource().getSourceName())
                .userName(lead.getUser().getFullName())
                .build();
    }
}
