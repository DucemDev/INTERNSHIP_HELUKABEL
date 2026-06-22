package com.helu.internship.repo;

import com.helu.internship.dto.response.*;
import com.helu.internship.entity.LeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRepo extends JpaRepository<LeadEntity, String> {

    @Query(value = """
            SELECT
                l.lead_id AS leadId,
                l.created_date AS createdDate,
                l.full_name AS fullName,
                CAST(NULL AS VARCHAR(20)) AS phoneNumber,
                l.account AS account,
                l.industry_type AS industryType,
                l.customer_group AS customerGroup,
                l.customer_role AS customerRole,
                l.location AS location,
                l.region AS region,
                l.status AS status,
                l.cost AS cost,
                l.loss_reason AS lossReason,
                l.business_result AS businessResult,
                STRING_AGG(p.product_name, ', ') AS productName,
                ls.source_name AS sourceName,
                u.full_name AS userName
            FROM lead l
            LEFT JOIN lead_source ls ON l.source_id = ls.source_id
            LEFT JOIN [user] u ON l.user_id = u.user_id
            LEFT JOIN lead_item li ON l.lead_id = li.lead_id
            LEFT JOIN product p ON li.product_id = p.product_id
            GROUP BY
                l.lead_id,
                l.created_date,
                l.full_name,
                l.account,
                l.industry_type,
                l.customer_group,
                l.customer_role,
                l.location,
                l.region,
                l.status,
                l.cost,
                l.loss_reason,
                l.business_result,
                ls.source_name,
                u.full_name
            """, nativeQuery = true)
    List<LeadListProjection> findLeadList();

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
    List<LeadByStatusResponse> findLeadsByStatus(@Param("status") String status);

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
        industry_type AS industryType,

        CAST(SUM(
            CASE
                WHEN status IN ('Qualified','Won')
                THEN 1
                ELSE 0
            END
        ) AS BIGINT) AS qualifiedLead,

        CAST(SUM(
            CASE
                WHEN status = 'Won'
                THEN 1
                ELSE 0
            END
        ) AS BIGINT) AS wonLead,

        CAST((
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
        ) AS DECIMAL(18,2)) AS winRate

    FROM lead
    GROUP BY industry_type
    ORDER BY winRate DESC
    """, nativeQuery = true)
    List<WinRateByIndustryProjection> getWinRateByIndustry();

    @Query(value = """
    SELECT
        region AS region,

        CAST(SUM(
            CASE
                WHEN status IN ('Qualified','Won')
                THEN 1
                ELSE 0
            END
        ) AS BIGINT) AS qualifiedLead,

        CAST(SUM(
            CASE
                WHEN status = 'Won'
                THEN 1
                ELSE 0
            END
        ) AS BIGINT) AS wonLead,

        CAST((
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
        ) AS DECIMAL(18,2)) AS winRate

    FROM lead
    GROUP BY region
    ORDER BY winRate DESC
    """, nativeQuery = true)
    List<WinRateByRegionProjection> getWinRateByRegion();
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

    @Query(value = """
            SELECT
                l.lead_id AS leadId,
                l.created_date AS createdDate,
                l.full_name AS fullName,
                CAST(NULL AS VARCHAR(20)) AS phoneNumber,
                l.account AS account,
                l.industry_type AS industryType,
                l.customer_group AS customerGroup,
                l.customer_role AS customerRole,
                l.location AS location,
                l.region AS region,
                l.status AS status,
                l.cost AS cost,
                l.loss_reason AS lossReason,
                l.business_result AS businessResult,
                STRING_AGG(p.product_name, ', ') AS productName,
                ls.source_name AS sourceName,
                u.full_name AS userName
            FROM lead l
            LEFT JOIN lead_source ls ON l.source_id = ls.source_id
            LEFT JOIN [user] u ON l.user_id = u.user_id
            LEFT JOIN lead_item li ON l.lead_id = li.lead_id
            LEFT JOIN product p ON li.product_id = p.product_id
            WHERE l.lead_id = :id
            GROUP BY
                l.lead_id,
                l.created_date,
                l.full_name,
                l.account,
                l.industry_type,
                l.customer_group,
                l.customer_role,
                l.location,
                l.region,
                l.status,
                l.cost,
                l.loss_reason,
                l.business_result,
                ls.source_name,
                u.full_name
            """, nativeQuery = true)
    Optional<LeadListProjection> findLeadByIdForView(@Param("id") String id);
    @Query(value = """
    SELECT
        l.loss_reason AS lossReason,
        COUNT(DISTINCT l.lead_id) AS totalLost
    FROM lead l
    INNER JOIN lead_item li
        ON l.lead_id = li.lead_id
    INNER JOIN product p
        ON li.product_id = p.product_id
    WHERE l.status = 'Lost'
        AND l.loss_reason IS NOT NULL
        AND (:productId IS NULL OR p.product_id = :productId)
    GROUP BY l.loss_reason
    ORDER BY totalLost DESC
    """, nativeQuery = true)

//    List<LostReasonSummaryProjection> getLostReasonSummary();
    List<LostReasonSummaryProjection> getLostReasonSummary(
            @Param("productId") String productId
    );

    @Query(value = """
            SELECT
                                            ls.source_name AS label,
                                            COUNT(*) AS totalLead,
                                            SUM(CASE WHEN l.status = 'Won' THEN 1 ELSE 0 END) AS wonLead,
                                            SUM(CASE WHEN l.status = 'Won' THEN 1 ELSE 0 END) * 100.0 / COUNT(*) AS conversionRate
                                        FROM lead l
                                        JOIN lead_source ls
                                            ON l.source_id = ls.source_id
                                        WHERE (:sourceId IS NULL OR l.source_id = :sourceId)
                                          AND (:sourceType IS NULL OR ls.source_type = :sourceType)
                                          AND (:region IS NULL OR l.region = :region)
                                          AND (:industry IS NULL OR l.industry_type = :industry)
                                          AND (:salesOwnerId IS NULL OR CAST(l.user_id AS VARCHAR(36)) = :salesOwnerId)
                                          AND (:customerGroup IS NULL OR l.customer_group = :customerGroup)
                                          AND (:timeFrom IS NULL OR l.created_date >= :timeFrom)
                                          AND (:timeTo IS NULL OR l.created_date <= :timeTo)
            
                                        GROUP BY ls.source_name
                                        ORDER BY conversionRate DESC
""", nativeQuery = true)
    List<ConversionRateResponse> getConversionRateFilter(
            @Param("sourceId") String sourceId,
            @Param("sourceType") String sourceType,
            @Param("region") String region,
            @Param("industry") String industry,
            @Param("salesOwnerId") String salesOwnerId,
            @Param("customerGroup") String customerGroup,
            @Param("timeFrom") LocalDate timeFrom,
            @Param("timeTo") LocalDate timeTo
    );

    @Query(value = """
    SELECT
        ls.source_name AS label,

        CAST(SUM(CASE WHEN l.status = 'Won' THEN 1 ELSE 0 END) AS BIGINT) AS wonLead,

        SUM(l.cost) AS totalCost,

        SUM(CASE 
            WHEN l.status = 'Won' 
            THEN l.business_result 
            ELSE 0 
        END) AS totalWonValue,

        (
            SUM(CASE 
                WHEN l.status = 'Won' 
                THEN l.business_result 
                ELSE 0 
            END) * 1.0
            /
            NULLIF(SUM(l.cost), 0)
        ) AS roi

    FROM lead l
    JOIN lead_source ls
        ON l.source_id = ls.source_id

    GROUP BY ls.source_name

    ORDER BY roi DESC
""", nativeQuery = true)
    List<RoiLeadSourceResponse> getROIByLeadSource();
    @Query(value = """
    SELECT
        l.industry_type AS industry,

        SUM(l.business_result) AS revenue
        FROM lead l
        
            GROUP BY l.industry_type
        
            ORDER BY revenue DESC
        """, nativeQuery = true)
            List<RevenueIndustryResponse> getRevenueByIndustry();
        



}


