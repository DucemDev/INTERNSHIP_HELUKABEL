package com.helu.internship.repo;

import com.helu.internship.dto.response.LeadSourceCostProjection;
import com.helu.internship.entity.LeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CostPerLeadRepo extends JpaRepository<LeadEntity, String> {
    @Query(value = """
    SELECT
        ls.source_id AS sourceId,
        ls.source_name AS sourceName,
        CAST(SUM(l.cost) AS DECIMAL(18,2)) AS totalCost,
        CAST(COUNT(l.lead_id) AS BIGINT) AS totalLead,
        CAST(SUM(l.cost) / NULLIF(COUNT(l.lead_id), 0) AS DECIMAL(18,2)) AS costPerLead
    FROM lead l
    JOIN lead_source ls
        ON l.source_id = ls.source_id
    GROUP BY ls.source_id, ls.source_name
    """, nativeQuery = true)
    List<LeadSourceCostProjection> getCostPerLeadBySource();
}
