package com.helu.internship.repo;

import com.helu.internship.entity.LeadSourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadSourceRepo extends JpaRepository<LeadSourceEntity, String> {
}