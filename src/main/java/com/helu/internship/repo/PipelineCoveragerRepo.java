package com.helu.internship.repo;

import com.helu.internship.dto.response.PipelineCoverageProjection;
import com.helu.internship.entity.LeadStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PipelineCoveragerRepo extends JpaRepository<LeadStatusHistoryEntity, Long> {
    @Query(value = """
    SELECT
        u.user_code AS userCode,
        u.full_name AS salesOwner,
        st.period_month AS periodMonth,
        st.period_year AS periodYear,

        ISNULL(SUM(li.expected_revenue),0) AS openPipeline,

        st.revenue_target AS targetRevenue,

        CASE
            WHEN st.revenue_target = 0 THEN 0
            ELSE CAST(
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

    WHERE (:sellerCode IS NULL OR u.user_code = :sellerCode)

    GROUP BY
        u.user_code,
        u.full_name,
        st.period_month,
        st.period_year,
        st.revenue_target

    ORDER BY pipelineCoverage DESC
    """, nativeQuery = true)
    List<PipelineCoverageProjection> getPipelineCoverage(
            @Param("sellerCode") String sellerCode
    );

    @Query(value = """
    SELECT
        u.user_code AS userCode,
        u.full_name AS salesOwner,
        0 AS periodMonth,
        0 AS periodYear,

        ISNULL(SUM(li.expected_revenue),0) AS openPipeline,

        ISNULL(SUM(st.revenue_target),0) AS targetRevenue,

        CASE
            WHEN ISNULL(SUM(st.revenue_target),0) = 0 THEN 0
            ELSE CAST(
                ISNULL(SUM(li.expected_revenue),0) * 100.0
                / SUM(st.revenue_target)
            AS DECIMAL(18,2))
        END AS pipelineCoverage

    FROM sales_target st

    INNER JOIN [user] u
        ON st.user_id = u.user_id

    LEFT JOIN lead l
        ON u.user_id = l.user_id
        AND l.status NOT IN ('Won','Lost')
        AND (
            (:quarter = 'this' AND DATEPART(QUARTER, l.created_date) = DATEPART(QUARTER, GETDATE()) AND YEAR(l.created_date) = YEAR(GETDATE()))
            OR (:quarter = 'last' AND DATEPART(QUARTER, l.created_date) = DATEPART(QUARTER, DATEADD(QUARTER, -1, GETDATE())) AND YEAR(l.created_date) = YEAR(DATEADD(QUARTER, -1, GETDATE())))
        )

    LEFT JOIN lead_item li
        ON l.lead_id = li.lead_id

    WHERE
        (:quarter = 'this' AND st.period_year = YEAR(GETDATE()) AND st.period_month IN ((DATEPART(QUARTER, GETDATE()) - 1) * 3 + 1, (DATEPART(QUARTER, GETDATE()) - 1) * 3 + 2, (DATEPART(QUARTER, GETDATE()) - 1) * 3 + 3))
        OR (:quarter = 'last' AND st.period_year = YEAR(DATEADD(QUARTER, -1, GETDATE())) AND st.period_month IN ((DATEPART(QUARTER, DATEADD(QUARTER, -1, GETDATE())) - 1) * 3 + 1, (DATEPART(QUARTER, DATEADD(QUARTER, -1, GETDATE())) - 1) * 3 + 2, (DATEPART(QUARTER, DATEADD(QUARTER, -1, GETDATE())) - 1) * 3 + 3))

    GROUP BY
        u.user_code,
        u.full_name

    ORDER BY pipelineCoverage DESC
    """, nativeQuery = true)
    List<PipelineCoverageProjection> getPipelineCoverageByQuarter(@Param("quarter") String quarter);
}
