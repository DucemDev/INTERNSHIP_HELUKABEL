package com.helu.internship.service.impl;

import com.helu.internship.dto.request.LeadRequest;
import com.helu.internship.dto.response.LeadListProjection;
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
    private final UserRepo userRepo;

    @Override
    @Transactional(readOnly = true)
    public List<LeadResponse> getAllLeads() {
        return leadRepo.findLeadList().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LeadResponse getLeadById(String id) {
        LeadListProjection lead = leadRepo.findLeadByIdForView(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        return mapToResponse(lead);
    }

    @Override
    @Transactional
    public LeadResponse createLead(LeadRequest request) {
        // Hỗ trợ trường hợp tạo Lead mà chưa gán user (userId truyền lên là null)
        UserEntity user = null;
        if (request.getUserId() != null) {
            user = userRepo.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

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
                .productName(request.getProductName())
                .sourceName(request.getSourceName())
                .user(user)
                .build();

        return mapToResponse(leadRepo.save(lead));
    }

    @Override
    @Transactional
    public LeadResponse updateLead(String id, LeadRequest request) {
        LeadEntity lead = leadRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

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
        lead.setProductName(request.getProductName());
        lead.setSourceName(request.getSourceName());

        return mapToResponse(leadRepo.save(lead));
    }

    @Override
    @Transactional
    public void deleteLead(String id) {
        leadRepo.deleteById(id);
    }

    // MAPPER AN TOÀN TUYỆT ĐỐI (Phòng thủ mọi NullPointerException)
    private LeadResponse mapToResponse(LeadListProjection lead) {
        return LeadResponse.builder()
                .leadId(lead.getLeadId())
                .createdDate(lead.getCreatedDate())
                .fullName(lead.getFullName())
                .phoneNumber(lead.getPhoneNumber())
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
                .productName(lead.getProductName())
                .sourceName(lead.getSourceName())
                .userName(lead.getUserName())
                .build();
    }
    private LeadResponse mapToResponse(LeadEntity lead) {
        String userName = null;

        // Kiểm tra an toàn xem có user hay không
        if (lead.getUser() != null) {
            userName = lead.getUser().getFullName();
        }

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
                .productName(lead.getProductName())
                .sourceName(lead.getSourceName())
                .userName(userName) // Đã được xử lý Null an toàn ở trên
                .build();
    }
}
