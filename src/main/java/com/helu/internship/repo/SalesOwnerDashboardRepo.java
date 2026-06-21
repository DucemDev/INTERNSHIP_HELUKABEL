package com.helu.internship.repo;

import com.helu.internship.dto.response.SalesOwnerDashboardProjection;
import com.helu.internship.entity.LeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesOwnerDashboardRepo extends JpaRepository<LeadEntity, String> {

    @Query(value = """
        SELECT
            u.user_id AS userId,
            u.full_name AS userName,
            CAST(COUNT(l.lead_id) AS BIGINT) AS totalLead,
            CAST(SUM(CASE WHEN l.status = 'Won' THEN 1 ELSE 0 END) AS BIGINT) AS wonLead,
            CAST(SUM(CASE WHEN l.status NOT IN ('Won', 'Lost') THEN 1 ELSE 0 END) AS BIGINT) AS openLead,
            CAST(ISNULL(SUM(CASE WHEN l.status = 'Won' THEN l.business_result ELSE 0 END), 0) AS DECIMAL(18,2)) AS totalRevenue,
            CAST(
                SUM(CASE WHEN l.status = 'Won' THEN 1 ELSE 0 END) * 100.0
                / NULLIF(COUNT(l.lead_id), 0)
                AS DECIMAL(18,2)
            ) AS winRate,
            CAST(ISNULL(AVG(CASE
                WHEN l.status = 'Won' AND won_stage.changed_at IS NOT NULL
                THEN DATEDIFF(DAY, l.created_date, won_stage.changed_at)
                ELSE NULL
            END), 0) AS DECIMAL(18,2)) AS avgDaysToWon
        FROM lead l
        JOIN [user] u ON l.user_id = u.user_id
        OUTER APPLY (
            SELECT TOP 1 h.changed_at
            FROM lead_status_history h
            WHERE h.lead_id = l.lead_id
              AND h.new_status = 'Won'
            ORDER BY h.changed_at ASC
        ) won_stage
        GROUP BY u.user_id, u.full_name
        ORDER BY totalRevenue DESC
        """, nativeQuery = true)
    List<SalesOwnerDashboardProjection> getSalesOwnerDashboard();
}