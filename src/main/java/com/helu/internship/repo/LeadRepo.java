package com.helu.internship.repo;

import com.helu.internship.dto.response.LeadByStatusResponse;
import com.helu.internship.entity.LeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepo extends JpaRepository<LeadEntity, String> {
    @Query("SELECT l.status, COUNT(l) FROM LeadEntity l GROUP BY l.status")
    List<Object[]> countLeadByStatus();
    @Query("""
        SELECT new com.helu.internship.dto.response.LeadByStatusResponse(
            l.leadId,
            l.fullName,
            l.account,
            l.status,
            l.region,
            l.industryType,
            l.customerGroup
        )
        FROM LeadEntity l
        WHERE l.status = :status
        """)
    List<LeadByStatusResponse> findLeadsByStatus(String status);
}
