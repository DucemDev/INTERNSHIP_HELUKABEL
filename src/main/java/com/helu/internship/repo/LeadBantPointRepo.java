package com.helu.internship.repo;

import com.helu.internship.entity.LeadBantPointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadBantPointRepo extends JpaRepository<LeadBantPointEntity, String> {
}
