package com.helu.internship.repo;

import com.helu.internship.entity.SalesTargetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalesTargetRepo extends JpaRepository<SalesTargetEntity, Long> {
    List<SalesTargetEntity> findByUser_UserId(UUID userId);
    Optional<SalesTargetEntity> findByUser_UserIdAndPeriodMonthAndPeriodYear(UUID userId, Integer periodMonth, Integer periodYear);
    List<SalesTargetEntity> findByUser_UserIdAndPeriodYear(UUID userId, Integer periodYear);
}
