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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.helu.internship.dto.response.LeadRevenueProjection;

@Service
@RequiredArgsConstructor
public class LeadImpl implements LeadService {

    private final LeadRepo leadRepo;
    private final UserRepo userRepo;
    private final LeadBantPointRepo leadBantPointRepo;

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

        Integer budget = null;
        Integer authority = null;
        Integer need = null;
        Integer timeline = null;
        Integer totalScore = null;
        String leadHeat = "COLD"; // Mặc định nếu chưa chấm điểm

        if (lead.getBantPoint() != null) {
            LeadBantPointEntity bp = lead.getBantPoint();
            budget = bp.getBudget();
            authority = bp.getAuthority();
            need = bp.getNeed();
            timeline = bp.getTimeline();
            totalScore = bp.getTotalScore();
            if (totalScore == null) {
                totalScore = (budget != null ? budget : 0) +
                             (authority != null ? authority : 0) +
                             (need != null ? need : 0) +
                             (timeline != null ? timeline : 0);
            }
            if (totalScore >= 80) {
                leadHeat = "HOT";
            } else if (totalScore >= 60) {
                leadHeat = "WARM";
            } else {
                leadHeat = "COLD";
            }
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
                .userName(userName)
                .bantBudget(budget)
                .bantAuthority(authority)
                .bantNeed(need)
                .bantTimeline(timeline)
                .bantTotalScore(totalScore)
                .leadHeat(leadHeat)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadResponse> getSellerLeads(String email, String heatFilter) {
        List<LeadEntity> leads = leadRepo.findBySellerEmail(email);

        List<LeadRevenueProjection> revs = leadRepo.getLeadsExpectedRevenue();
        Map<String, BigDecimal> revenueMap = revs.stream()
                .collect(Collectors.toMap(
                    LeadRevenueProjection::getLeadId,
                    p -> p.getExpectedRevenue() != null ? p.getExpectedRevenue() : BigDecimal.ZERO,
                    (a, b) -> a
                ));

        return leads.stream()
                .map(lead -> {
                    LeadResponse res = this.mapToResponse(lead);
                    BigDecimal expected = revenueMap.getOrDefault(lead.getLeadId(), BigDecimal.ZERO);
                    if (lead.getStatus() != null && lead.getStatus().trim().equalsIgnoreCase("Won") && lead.getBusinessResult() != null) {
                        res.setExpectedRevenue(lead.getBusinessResult());
                    } else {
                        res.setExpectedRevenue(expected);
                    }
                    return res;
                })
                .filter(res -> {
                    if (heatFilter == null || heatFilter.trim().isEmpty()) {
                        return true;
                    }
                    return heatFilter.equalsIgnoreCase(res.getLeadHeat());
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeadResponse updateSellerLeadStatus(String email, String leadId, String status) {
        LeadEntity lead = leadRepo.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found with id: " + leadId));
        if (lead.getUser() == null || !lead.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("You do not have permission to update this lead");
        }
        lead.setStatus(status);
        return mapToResponse(leadRepo.save(lead));
    }

    @Override
    @Transactional
    public LeadResponse updateSellerLeadBant(String email, String leadId, int budget, int authority, int need, int timeline) {
        if (budget < 0 || budget > 25 || authority < 0 || authority > 25 ||
            need < 0 || need > 25 || timeline < 0 || timeline > 25) {
            throw new IllegalArgumentException("Each BANT component score must be between 0 and 25 (inclusive)");
        }
        LeadEntity lead = leadRepo.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found with id: " + leadId));
        if (lead.getUser() == null || !lead.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("You do not have permission to update this lead");
        }
        LeadBantPointEntity bp = lead.getBantPoint();
        if (bp == null) {
            bp = LeadBantPointEntity.builder()
                    .leadId(leadId)
                    .lead(lead)
                    .build();
            lead.setBantPoint(bp);
        }
        bp.setBudget(budget);
        bp.setAuthority(authority);
        bp.setNeed(need);
        bp.setTimeline(timeline);
        leadRepo.save(lead);
        return mapToResponse(lead);
    }
}
