package com.helu.internship.api;

import com.helu.internship.dto.request.BantRequest;
import com.helu.internship.dto.response.LeadResponse;
import com.helu.internship.service.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/seller/leads")
@RequiredArgsConstructor
public class SellerLeadController {

    private final LeadService leadService;

    @GetMapping
    public ResponseEntity<List<LeadResponse>> getMyLeads(
            Principal principal,
            @RequestParam(required = false) String heat
    ) {
        String email = principal.getName();
        return ResponseEntity.ok(leadService.getSellerLeads(email, heat));
    }

    @PutMapping("/{leadId}/status")
    public ResponseEntity<LeadResponse> updateStatus(
            Principal principal,
            @PathVariable String leadId,
            @RequestParam String status
    ) {
        String email = principal.getName();
        return ResponseEntity.ok(leadService.updateSellerLeadStatus(email, leadId, status));
    }

    @PutMapping("/{leadId}/bant")
    public ResponseEntity<LeadResponse> updateBant(
            Principal principal,
            @PathVariable String leadId,
            @RequestBody BantRequest request
    ) {
        String email = principal.getName();
        return ResponseEntity.ok(leadService.updateSellerLeadBant(
                email,
                leadId,
                request.getBudget() != null ? request.getBudget() : 0,
                request.getAuthority() != null ? request.getAuthority() : 0,
                request.getNeed() != null ? request.getNeed() : 0,
                request.getTimeline() != null ? request.getTimeline() : 0
        ));
    }
}
