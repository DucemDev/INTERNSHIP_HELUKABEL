package com.helu.internship.repo;

import com.helu.internship.entity.LeadStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadStatusHistoryRepo extends JpaRepository<LeadStatusHistoryEntity, Long> {

    @Query(value = """
        SELECT
            AVG(DATEDIFF(DAY, new_stage.changed_at, won_stage.changed_at))
        FROM lead_status_history new_stage
        JOIN lead_status_history won_stage
            ON new_stage.lead_id = won_stage.lead_id
        WHERE new_stage.new_status = 'New'
          AND won_stage.new_status = 'Won'
        """, nativeQuery = true)
    Double getAverageDaysToWon();
}