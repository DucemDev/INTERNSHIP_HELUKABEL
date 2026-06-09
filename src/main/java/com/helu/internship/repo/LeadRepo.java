package com.helu.internship.repo;

import com.helu.internship.entity.LeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadRepo extends JpaRepository<LeadEntity, String> {

}
