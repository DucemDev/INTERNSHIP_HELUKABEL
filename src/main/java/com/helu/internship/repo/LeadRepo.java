package com.helu.internship.repo;

import com.helu.internship.dto.response.ConversionRateResponse;
import com.helu.internship.dto.response.CostPerWinBySourceResponse;
import com.helu.internship.dto.response.WinRateBySalesResponse;
import com.helu.internship.entity.LeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepo extends JpaRepository<LeadEntity, String> {


    @Query("""
    SELECT
        COUNT(l) AS totalLead,
        SUM(CASE WHEN l.status = 'Won' THEN 1 ELSE 0 END) AS wonLead,
        SUM(CASE WHEN l.status = 'Won' THEN 1 ELSE 0 END) * 100.0 / COUNT(l) AS conversionRate
    FROM LeadEntity l
""")
    ConversionRateResponse getConversionRate();

    @Query(value = """
    SELECT
        user_id AS userId,

        SUM(
            CASE
                WHEN status IN ('Qualified','Won')
                THEN 1
                ELSE 0
            END
        ) AS qualifiedLead,

        SUM(
            CASE
                WHEN status = 'Won'
                THEN 1
                ELSE 0
            END
        ) AS wonLead,

        (
            SUM(
                CASE
                    WHEN status = 'Won'
                    THEN 1
                    ELSE 0
                END
            ) * 100.0
            /
            NULLIF(
                SUM(
                    CASE
                        WHEN status IN ('Qualified','Won')
                        THEN 1
                        ELSE 0
                    END
                ),
                0
            )
        ) AS winRate

    FROM lead
    GROUP BY user_id
""", nativeQuery = true)
    List<WinRateBySalesResponse> getWinRateBySalesOwner();

    @Query(value = """
    SELECT
        ls.source_id AS sourceId,
        ls.source_name AS sourceName,
        SUM(l.cost) AS totalCost,
        SUM(CASE WHEN l.status = 'Won' THEN 1 ELSE 0 END) AS wonLead,
        SUM(l.cost) * 1.0 /
        NULLIF(SUM(CASE WHEN l.status = 'Won' THEN 1 ELSE 0 END), 0) AS costPerWin
    FROM lead l
    JOIN lead_source ls
        ON l.source_id = ls.source_id
    GROUP BY ls.source_id, ls.source_name
    ORDER BY costPerWin ASC
""", nativeQuery = true)
    List<CostPerWinBySourceResponse> getCostPerWinByLeadSource();
}
