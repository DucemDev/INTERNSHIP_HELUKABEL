package com.helu.internship.api;

import com.helu.internship.dto.request.LeadRequest;
import com.helu.internship.dto.response.LeadResponse;
import com.helu.internship.service.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @GetMapping
    public ResponseEntity<List<LeadResponse>> getAllLeads() {
        return ResponseEntity.ok(leadService.getAllLeads());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeadResponse> getLeadById(@PathVariable String id) {
        return ResponseEntity.ok(leadService.getLeadById(id));
    }

    @PostMapping
    public ResponseEntity<LeadResponse> createLead(@RequestBody LeadRequest request) {
        return ResponseEntity.ok(leadService.createLead(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeadResponse> updateLead(@PathVariable String id, @RequestBody LeadRequest request) {
        return ResponseEntity.ok(leadService.updateLead(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLead(@PathVariable String id) {
        leadService.deleteLead(id);
        return ResponseEntity.noContent().build();
    }
}
