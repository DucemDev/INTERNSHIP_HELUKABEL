package com.helu.internship.service;

import com.helu.internship.dto.request.SalesTargetRequest;
import com.helu.internship.dto.response.SalesTargetResponse;

import java.util.List;
import java.util.UUID;

public interface SalesTargetService {
    List<SalesTargetResponse> getTargetsByUser(UUID userId);
    List<SalesTargetResponse> getTargetsByUserAndYear(UUID userId, Integer periodYear);
    SalesTargetResponse saveOrUpdateTarget(SalesTargetRequest request, String adminEmail);
    void deleteTarget(Long targetId);
}
