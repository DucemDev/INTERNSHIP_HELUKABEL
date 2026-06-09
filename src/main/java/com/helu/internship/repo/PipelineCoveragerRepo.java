package com.helu.internship.repo;

import com.helu.internship.dto.response.PipelineCoverageProjection;
import com.helu.internship.entity.LeadStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PipelineCoveragerRepo extends JpaRepository<LeadStatusHistoryEntity, Long> {
    @Query(value = """
        SELECT
            u.user_code AS userCode,
            u.full_name AS salesOwner,

            ISNULL(SUM(li.expected_revenue),0) AS openPipeline,

            st.revenue_target AS target,

            CASE
                WHEN st.revenue_target = 0 THEN 0
                ELSE
                    CAST(
                        ISNULL(SUM(li.expected_revenue),0)
                        / st.revenue_target
                    AS DECIMAL(18,2))
            END AS pipelineCoverage

        FROM sales_target st

        INNER JOIN [user] u
            ON st.user_id = u.user_id

        LEFT JOIN lead l
            ON u.user_id = l.user_id
            AND l.status NOT IN ('Won','Lost')

        LEFT JOIN lead_item li
            ON l.lead_id = li.lead_id

        GROUP BY
            u.user_code,
            u.full_name,
            st.revenue_target

        ORDER BY pipelineCoverage DESC
        """, nativeQuery = true)
    List<PipelineCoverageProjection> getPipelineCoverage();
}