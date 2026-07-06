package com.helu.internship.service.impl;

import com.helu.internship.dto.request.SalesTargetRequest;
import com.helu.internship.dto.response.SalesTargetResponse;
import com.helu.internship.entity.SalesTargetEntity;
import com.helu.internship.entity.UserEntity;
import com.helu.internship.repo.SalesTargetRepo;
import com.helu.internship.repo.UserRepo;
import com.helu.internship.service.SalesTargetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesTargetServiceImpl implements SalesTargetService {

    private final SalesTargetRepo salesTargetRepo;
    private final UserRepo userRepo;

    @Override
    public List<SalesTargetResponse> getTargetsByUser(UUID userId) {
        return salesTargetRepo.findByUser_UserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SalesTargetResponse> getTargetsByUserAndYear(UUID userId, Integer periodYear) {
        return salesTargetRepo.findByUser_UserIdAndPeriodYear(userId, periodYear).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SalesTargetResponse saveOrUpdateTarget(SalesTargetRequest request, String adminEmail) {
        UserEntity seller = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        UserEntity admin = userRepo.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        Optional<SalesTargetEntity> existingTargetOpt = salesTargetRepo
                .findByUser_UserIdAndPeriodMonthAndPeriodYear(request.getUserId(), request.getPeriodMonth(), request.getPeriodYear());

        SalesTargetEntity targetEntity;
        if (existingTargetOpt.isPresent()) {
            targetEntity = existingTargetOpt.get();
            targetEntity.setRevenueTarget(request.getRevenueTarget());
            targetEntity.setCreatedBy(admin);
        } else {
            targetEntity = SalesTargetEntity.builder()
                    .user(seller)
                    .periodMonth(request.getPeriodMonth())
                    .periodYear(request.getPeriodYear())
                    .revenueTarget(request.getRevenueTarget())
                    .createdBy(admin)
                    .build();
        }

        return mapToResponse(salesTargetRepo.save(targetEntity));
    }

    @Override
    @Transactional
    public void deleteTarget(Long targetId) {
        salesTargetRepo.deleteById(targetId);
    }

    private SalesTargetResponse mapToResponse(SalesTargetEntity entity) {
        return SalesTargetResponse.builder()
                .targetId(entity.getTargetId())
                .userId(entity.getUser().getUserId())
                .userCode(entity.getUser().getUserCode())
                .fullName(entity.getUser().getFullName())
                .periodMonth(entity.getPeriodMonth())
                .periodYear(entity.getPeriodYear())
                .revenueTarget(entity.getRevenueTarget())
                .createdById(entity.getCreatedBy().getUserId())
                .createdByName(entity.getCreatedBy().getFullName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
