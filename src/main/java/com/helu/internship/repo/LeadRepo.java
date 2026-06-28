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

    @Query("SELECT l FROM LeadEntity l JOIN FETCH l.user u LEFT JOIN FETCH l.bantPoint WHERE u.email = :email")
    List<LeadEntity> findBySellerEmail(@Param("email") String email);

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
                l.customerGroup,
                CAST(NULL AS string),
                CAST(NULL AS string),
                l.user.fullName,
                l.createdDate
            )
            FROM LeadEntity l
            WHERE l.status = :status
            """)
    List<LeadByStatusResponse> findLeadsByStatus(@Param("status") String status);

    @Query("""
            SELECT
                COUNT(l) AS totalLead,
                SUM(CASE WHEN l.status = 'Won' THEN 1 ELSE 0 END) AS wonLead,
                SUM(CASE WHEN l.status = 'Won' THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(l), 0) AS conversionRate
            FROM LeadEntity l
            """)
    ConversionRateResponse getConversionRate();

    @Query(value = """
            SELECT
                l.user_id AS userId,
                u.full_name AS userName,
                SUM(
                    CASE
                        WHEN l.status IN ('Qualified','Won')
                        THEN 1
                        ELSE 0
                    END
                ) AS qualifiedLead,
                SUM(
                    CASE
                        WHEN l.status = 'Won'
                        THEN 1
                        ELSE 0
                    END
                ) AS wonLead,
                (
                    SUM(
                        CASE
                            WHEN l.status = 'Won'
                            THEN 1
                            ELSE 0
                        END
                    ) * 100.0
                    /
                    NULLIF(
                        SUM(
                            CASE
                                WHEN l.status IN ('Qualified','Won')
                                THEN 1
                                ELSE 0
                            END
                        ),
                        0
                    )
                ) AS winRate
            FROM lead l
            LEFT JOIN [user] u ON l.user_id = u.user_id
            WHERE (:region IS NULL OR l.region = :region)
              AND (:industry IS NULL OR l.industry_type = :industry)
            GROUP BY l.user_id, u.full_name
            """, nativeQuery = true)
    List<WinRateBySalesResponse> getWinRateBySalesOwner(
            @Param("region") String region,
            @Param("industry") String industry
    );

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
    List<LostReasonSummaryProjection> getLostReasonSummary(
            @Param("productId") String productId
    );

    @Query(value = """
                SELECT
                    ls.source_name AS label,
                    l.region AS region,
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
                GROUP BY ls.source_name, l.region, l.customer_group
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

    @Query("""
            SELECT
                COUNT(l) AS totalLead,
                SUM(CASE WHEN l.status = 'Won' THEN 1 ELSE 0 END) AS wonLead,
                SUM(CASE WHEN l.status = 'Won' THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(l), 0) AS conversionRate
            FROM LeadEntity l
            WHERE l.user.email = :email 
            """)
    ConversionRateResponse getStatsByEmail(@Param("email") String email);

    @Query(value = """
            SELECT
                l.industry_type AS industry,
            
                SUM(l.business_result) AS revenue
                FROM lead l
            
                    GROUP BY l.industry_type
            
                    ORDER BY revenue DESC
            """, nativeQuery = true)
    List<RevenueIndustryResponse> getRevenueByIndustry();

    @Query(value = """
            SELECT
                CAST(ISNULL(SUM(business_result),0) AS DECIMAL(18,2)) AS totalRevenue,
                CAST(COUNT(*) AS BIGINT) AS wonLead,
                CAST(
                    ISNULL(SUM(business_result),0)
                    /
                    NULLIF(COUNT(*),0)
                    AS DECIMAL(18,2)
                ) AS avgRevenuePerWonLead
            FROM lead
            WHERE status = 'Won'
            """, nativeQuery = true)
    RevenueSummaryProjection getRevenueSummary();

    @Query(value = """
            SELECT
                l.region AS region,
                CAST(ISNULL(SUM(l.business_result),0) AS DECIMAL(18,2)) AS revenue,
                CAST(COUNT(*) AS BIGINT) AS wonLead
            FROM lead l
            WHERE l.status = 'Won'
            GROUP BY l.region
            ORDER BY revenue DESC
            """, nativeQuery = true)
    List<RevenueRegionProjection> getRevenueByRegion();

    @Query(value = """
            SELECT
                p.product_id AS productId,
                p.product_name AS productName,
                CAST(ISNULL(SUM(li.expected_revenue),0) AS DECIMAL(18,2)) AS revenue,
                CAST(COUNT(DISTINCT l.lead_id) AS BIGINT) AS totalWonLead
            FROM lead l
            JOIN lead_item li
                ON l.lead_id = li.lead_id
            JOIN product p
                ON li.product_id = p.product_id
            WHERE l.status = 'Won'
            GROUP BY
                p.product_id,
                p.product_name
            ORDER BY revenue DESC
            """, nativeQuery = true)
    List<RevenueProductLineProjection> getRevenueByProductLine();

    @Query(value = """
            SELECT
                u.user_code AS userCode,
                u.full_name AS salesOwner,
                COUNT(CASE WHEN l.status = 'Lost' THEN 1 END) AS lostLead,
                CAST(
                    COUNT(CASE WHEN l.status = 'Lost' THEN 1 END) * 100.0
                    / NULLIF(COUNT(*),0)
                AS DECIMAL(18,2)
                ) AS lostRate
            FROM lead l
            JOIN [user] u
                ON l.user_id = u.user_id
            GROUP BY u.user_code, u.full_name
            ORDER BY lostRate DESC
            """, nativeQuery = true)
    List<LostBySellerProjection> getLostBySeller();

    @Query(value = """
            SELECT
                ls.source_id AS sourceId,
                ls.source_name AS sourceName,
                COUNT(CASE WHEN l.status = 'Lost' THEN 1 END) AS lostLead,
                CAST(
                    COUNT(CASE WHEN l.status = 'Lost' THEN 1 END) * 100.0
                    / NULLIF(COUNT(*),0)
                AS DECIMAL(18,2)
                ) AS lostRate
            FROM lead l
            JOIN lead_source ls
                ON l.source_id = ls.source_id
            GROUP BY ls.source_id, ls.source_name
            ORDER BY lostRate DESC
            """, nativeQuery = true)
    List<LostBySourceProjection> getLostBySource();

    @Query(value = """
            SELECT
                l.region AS region,
                COUNT(CASE WHEN l.status = 'Lost' THEN 1 END) AS lostLead,
                CAST(
                    COUNT(CASE WHEN l.status = 'Lost' THEN 1 END) * 100.0
                    / NULLIF(COUNT(*),0)
                AS DECIMAL(18,2)
                ) AS lostRate
            FROM lead l
            GROUP BY l.region
            ORDER BY lostRate DESC
            """, nativeQuery = true)
    List<LostByRegionProjection> getLostByRegion();

    @Query(value = """
            SELECT
                l.industry_type AS industryType,
                COUNT(CASE WHEN l.status = 'Lost' THEN 1 END) AS lostLead,
                CAST(
                    COUNT(CASE WHEN l.status = 'Lost' THEN 1 END) * 100.0
                    / NULLIF(COUNT(*),0)
                AS DECIMAL(18,2)
                ) AS lostRate
            FROM lead l
            GROUP BY l.industry_type
            ORDER BY lostRate DESC
            """, nativeQuery = true)
    List<LostByIndustryProjection> getLostByIndustry();

    @Query(value = """
            SELECT
                YEAR(l.created_date) AS year,
                MONTH(l.created_date) AS month,
                SUM(l.business_result) AS revenue
            FROM lead l
            WHERE l.status = 'Won'
            GROUP BY
                YEAR(l.created_date),
                MONTH(l.created_date)
            ORDER BY
                year,
                month
            """, nativeQuery = true)
    List<RevenueMonthlyProjection> getRevenueMonthly();

    @Query(value = """
            SELECT
                YEAR(l.created_date) AS year,
                DATEPART(QUARTER, l.created_date) AS quarter,
                SUM(l.business_result) AS revenue
            FROM lead l
            WHERE l.status = 'Won'
            GROUP BY
                YEAR(l.created_date),
                DATEPART(QUARTER, l.created_date)
            ORDER BY
                year,
                quarter
            """, nativeQuery = true)
    List<RevenueQuarterlyProjection> getRevenueQuarterly();

    @Query(value = """
            SELECT
                YEAR(l.created_date) AS year,
                MONTH(l.created_date) AS month,
            
                COUNT(*) AS totalLead,
            
                SUM(
                    CASE
                        WHEN l.status = 'Won'
                        THEN 1
                        ELSE 0
                    END
                ) AS wonLead,
            
                SUM(
                    CASE
                        WHEN l.status = 'Lost'
                        THEN 1
                        ELSE 0
                    END
                ) AS lostLead
            
            FROM lead l
            GROUP BY
                YEAR(l.created_date),
                MONTH(l.created_date)
            ORDER BY
                year,
                month
            """, nativeQuery = true)
    List<LeadMonthlyProjection> getLeadMonthly();

    @Query(value = """
            SELECT
                YEAR(l.created_date) AS year,
                DATEPART(QUARTER, l.created_date) AS quarter,
            
                COUNT(*) AS totalLead,
            
                SUM(
                    CASE
                        WHEN l.status = 'Won'
                        THEN 1
                        ELSE 0
                    END
                ) AS wonLead,
            
                SUM(
                    CASE
                        WHEN l.status = 'Lost'
                        THEN 1
                        ELSE 0
                    END
                ) AS lostLead
            
            FROM lead l
            GROUP BY
                YEAR(l.created_date),
                DATEPART(QUARTER, l.created_date)
            ORDER BY
                year,
                quarter
            """, nativeQuery = true)
    List<LeadQuarterlyProjection> getLeadQuarterly();

    @Query(value = """
            SELECT
                YEAR(l.created_date) AS year,
                MONTH(l.created_date) AS month,
                u.full_name AS sellerName,
                SUM(l.business_result) AS revenue
            FROM lead l
            JOIN [user] u
                ON l.user_id = u.user_id
            WHERE l.status = 'Won'
            GROUP BY
                YEAR(l.created_date),
                MONTH(l.created_date),
                u.full_name
            ORDER BY
                YEAR(l.created_date),
                MONTH(l.created_date)
            """, nativeQuery = true)
    List<RevenueSellerMonthlyProjection> getRevenueSellerMonthly();

    @Query(value = """
            SELECT
                YEAR(l.created_date) AS year,
                MONTH(l.created_date) AS month,
                ls.source_name AS leadSource,
                SUM(l.business_result) AS revenue
            FROM lead l
            JOIN lead_source ls
                ON l.source_id = ls.source_id
            WHERE l.status = 'Won'
            GROUP BY
                YEAR(l.created_date),
                MONTH(l.created_date),
                ls.source_name
            ORDER BY year, month
            """, nativeQuery = true)
    List<RevenueSourceMonthlyProjection> getRevenueSourceMonthly();

    @Query(value = """
            SELECT
                YEAR(created_date) AS year,
                MONTH(created_date) AS month,
                region AS region,
                SUM(business_result) AS revenue
            FROM lead
            WHERE status = 'Won'
            GROUP BY
                YEAR(created_date),
                MONTH(created_date),
                region
            ORDER BY year, month
            """, nativeQuery = true)
    List<RevenueRegionMonthlyProjection> getRevenueRegionMonthly();

    @Query(value = """
            SELECT
                YEAR(created_date) AS year,
                MONTH(created_date) AS month,
                industry_type AS industry,
                SUM(business_result) AS revenue
            FROM lead
            WHERE status = 'Won'
            GROUP BY
                YEAR(created_date),
                MONTH(created_date),
                industry_type
            ORDER BY year, month
            """, nativeQuery = true)
    List<RevenueIndustryMonthlyProjection> getRevenueIndustryMonthly();

    @Query(value = """
            SELECT
                YEAR(l.created_date) AS year,
                MONTH(l.created_date) AS month,
                p.product_name AS productLine,
                SUM(l.business_result) AS revenue
            FROM lead l
            JOIN lead_item li
                ON l.lead_id = li.lead_id
            JOIN product p
                ON li.product_id = p.product_id
            WHERE l.status = 'Won'
            GROUP BY
                YEAR(l.created_date),
                MONTH(l.created_date),
                p.product_name
            ORDER BY year, month
            """, nativeQuery = true)
    List<RevenueProductLineMonthlyProjection> getRevenueProductLineMonthly();

    @Query(value = """
                SELECT
                    l.account AS account,
            
                    CAST(
                        SUM(
                            CASE
                                WHEN l.status = 'Won'
                                THEN 1
                                ELSE 0
                            END
                        ) AS BIGINT
                    ) AS wonLead,
            
                    SUM(
                        CASE
                            WHEN l.status = 'Won'
                            THEN l.business_result
                            ELSE 0
                        END
                    ) AS totalRevenue
            
                FROM lead l
            
                GROUP BY l.account
            
                ORDER BY totalRevenue DESC
            """, nativeQuery = true)
    List<RevenueByAccountResponse> getRevenueByAccount();

    @Query(value = """
                SELECT
                    l.industry_type AS industryType,
            
                    COUNT(*) AS totalLead
            
                FROM lead l
            
                WHERE l.status IN (
                    'New',
                    'Contacted',
                    'Qualified',
                    'Proposal Sent',
                    'In Negotiation'
                )
            
                GROUP BY l.industry_type
            
                ORDER BY totalLead DESC
            """, nativeQuery = true)
    List<LeadByIndustryResponse> getPotentialLeadByIndustry();

    @Query(value = """
                SELECT
                    ls.source_name AS sourceName,
            
                    SUM(CASE WHEN l.status = 'New' THEN 1 ELSE 0 END) AS newLead,
            
                    SUM(CASE WHEN l.status = 'Contacted' THEN 1 ELSE 0 END) AS contactedLead,
            
                    SUM(CASE WHEN l.status = 'Qualified' THEN 1 ELSE 0 END) AS qualifiedLead,
            
                    SUM(CASE WHEN l.status = 'Proposal Sent' THEN 1 ELSE 0 END) AS proposalSentLead,
            
                    SUM(CASE WHEN l.status = 'In Negotiation' THEN 1 ELSE 0 END) AS negotiationLead
            
                FROM lead l
                JOIN lead_source ls
                    ON l.source_id = ls.source_id
            
                WHERE (:sourceId IS NULL OR l.source_id = :sourceId)
            
                GROUP BY ls.source_name
            
                ORDER BY ls.source_name
            """, nativeQuery = true)
    List<LeadFunnelBySourceResponse> getLeadFunnelBySource(
            @Param("sourceId") String sourceId
    );

    @Query(value = """
                SELECT
                    p.product_name AS productName,
                    ls.source_name AS sourceName,
                    COUNT(l.lead_id) AS totalLead
                FROM lead l
                JOIN product p
                    ON l.product_id = p.product_id
                JOIN lead_source ls
                    ON l.source_id = ls.source_id
                GROUP BY
                    p.product_name,
                    ls.source_name
                ORDER BY
                    p.product_name,
                    totalLead DESC
            """, nativeQuery = true)
    List<LeadSourceByProductProjection> getLeadSourceByProduct();

    @Query(value = """
            SELECT
                ls.source_name AS sourceName,
                p.product_name AS productLine,
                SUM(l.business_result) AS totalRevenue
            FROM lead l
            JOIN lead_source ls
                ON l.source_id = ls.source_id
            JOIN lead_item li
                ON l.lead_id = li.lead_id
            JOIN product p
                ON li.product_id = p.product_id
            WHERE l.status = 'Won'
            GROUP BY
                ls.source_name,
                p.product_name
            ORDER BY
                totalRevenue DESC
            """, nativeQuery = true)
    List<RevenueBySourceProductResponse> getRevenueBySourceProduct();

    @Query(value = """
            SELECT
                ls.source_name AS sourceName,
                p.product_name AS productLine,
                COUNT(DISTINCT l.lead_id) AS wonLead
            FROM lead l
            JOIN lead_source ls
                ON l.source_id = ls.source_id
            JOIN lead_item li
                ON l.lead_id = li.lead_id
            JOIN product p
                ON li.product_id = p.product_id
            WHERE l.status = 'Won'
            GROUP BY
                ls.source_name,
                p.product_name
            ORDER BY
                wonLead DESC
            """, nativeQuery = true)
    List<WonLeadBySourceProductResponse> getWonLeadBySourceProduct();

    @Query(value = """
                SELECT
                    ls.source_name AS sourceName,
            
                    p.product_name AS productName,
            
                    l.loss_reason AS lossReason,
            
                    COUNT(DISTINCT l.lead_id) AS totalLost
            
                FROM lead l
            
                JOIN lead_source ls
                    ON l.source_id = ls.source_id
            
                JOIN lead_item li
                    ON l.lead_id = li.lead_id
            
                JOIN product p
                    ON li.product_id = p.product_id
            
                WHERE l.status = 'Lost'
                  AND l.loss_reason IS NOT NULL
            
                GROUP BY
                    ls.source_name,
                    p.product_name,
                    l.loss_reason
            
                ORDER BY totalLost DESC
            """, nativeQuery = true)
    List<LostLeadBySourceResponse> getLostLeadBySource();

    @Query(value = """
    SELECT TOP 1
        l.account AS account,

        COUNT(*) AS wonDeal,

        SUM(l.business_result) AS totalRevenue

    FROM lead l

    WHERE l.status = 'Won'

    GROUP BY l.account

    ORDER BY totalRevenue DESC
""", nativeQuery = true)
    BestAccountRevenueResponse getBestAccountByRevenue();

    @Query(value = """
    SELECT TOP 1
        l.industry_type AS industryType,

        COUNT(*) AS wonDeal

    FROM lead l

    WHERE l.status = 'Won'

    GROUP BY l.industry_type

    ORDER BY wonDeal DESC
""", nativeQuery = true)
    BestIndustryByWonDealResponse getBestIndustryByWonDeal();

    @Query(value = """
    SELECT TOP 1
        l.industry_type AS industryType,

        SUM(l.business_result) AS totalRevenue

    FROM lead l

    WHERE l.status = 'Won'

    GROUP BY l.industry_type

    ORDER BY totalRevenue DESC
""", nativeQuery = true)
    BestIndustryByRevenueResponse getBestIndustryByRevenue();

    @Query(value = """
    SELECT TOP 1
        l.region AS region,

        COUNT(*) AS wonDeal

    FROM lead l

    WHERE l.status = 'Won'

    GROUP BY l.region

    ORDER BY wonDeal DESC
""", nativeQuery = true)
    BestRegionByWonDealResponse getBestRegionByWonDeal();

    @Query(value = """
    SELECT TOP 1
        l.region AS region,

        SUM(l.business_result) AS totalRevenue

    FROM lead l

    WHERE l.status = 'Won'

    GROUP BY l.region

    ORDER BY totalRevenue DESC
""", nativeQuery = true)
    BestRegionByRevenueResponse getBestRegionByRevenue();

    @Query(value = """
    SELECT TOP 1
        l.customer_group AS customerGroup,

        COUNT(*) AS totalLead

    FROM lead l

    GROUP BY l.customer_group

    ORDER BY totalLead DESC
""", nativeQuery = true)
    BestCustomerGroupByLeadResponse getBestCustomerGroupByLead();

    @Query(value = """
    SELECT TOP 1
        l.customer_group AS customerGroup,

        SUM(l.business_result) AS totalRevenue

    FROM lead l

    WHERE l.status = 'Won'

    GROUP BY l.customer_group

    ORDER BY totalRevenue DESC
""", nativeQuery = true)
    BestCustomerGroupByRevenueResponse getBestCustomerGroupByRevenue();

    @Query(value = """
    SELECT
        l.industry_type AS industryType,
        COUNT(*) AS totalLead
    FROM lead l
    WHERE (:industry IS NULL OR l.industry_type = :industry)
    GROUP BY l.industry_type
    """, nativeQuery = true)
    TotalLeadByIndustryResponse getTotalLeadByIndustry(
            @Param("industry") String industry
    );

    @Query(value = """
    SELECT
        l.industry_type AS industry,
        COUNT(*) AS wonLead
    FROM lead l
    WHERE l.status = 'Won'
    GROUP BY l.industry_type
    ORDER BY wonLead DESC
    """, nativeQuery = true)
    List<WonLeadByIndustryResponse> getWonLeadByIndustry();

    @Query(value = """
    SELECT
        l.industry_type AS industry,

        COUNT(*) AS totalLead,

        SUM(
            CASE
                WHEN l.status = 'Won' THEN 1
                ELSE 0
            END
        ) AS wonLead,

        ROUND(
            SUM(
                CASE
                    WHEN l.status = 'Won' THEN 1
                    ELSE 0
                END
            ) * 100.0 / COUNT(*),
            2
        ) AS conversionRate

    FROM lead l

    GROUP BY l.industry_type

    ORDER BY conversionRate DESC
    """, nativeQuery = true)
    List<IndustryConversionRateResponse> getIndustryConversionRate();

    @Query(value = """
    SELECT
        l.industry_type AS industry,

        ROUND(
            AVG(
                DATEDIFF(
                    DAY,
                    l.created_date,
                    CAST(h.changed_at AS DATE)
                ) * 1.0
            ),
            2
        ) AS avgSalesCycle

    FROM lead l

    JOIN lead_status_history h
        ON l.lead_id = h.lead_id

    WHERE h.new_status = 'Won'

    GROUP BY l.industry_type

    ORDER BY avgSalesCycle ASC
    """, nativeQuery = true)
    List<AvgSalesCycleByIndustryResponse> getAvgSalesCycleByIndustry();

    @Query(value = """
    SELECT
        t.industry AS industry,
        t.lossReason AS lossReason,
        t.lostLead AS lostLead
    FROM (
        SELECT
            l.industry_type AS industry,
            l.loss_reason AS lossReason,
            COUNT(*) AS lostLead,
            ROW_NUMBER() OVER (
                PARTITION BY l.industry_type
                ORDER BY COUNT(*) DESC
            ) AS rn
        FROM lead l
        WHERE l.status = 'Lost'
          AND l.loss_reason IS NOT NULL
        GROUP BY l.industry_type, l.loss_reason
    ) t
    WHERE t.rn = 1
    ORDER BY t.lostLead DESC
    """, nativeQuery = true)
    List<BestLostReasonByIndustryResponse> getBestLostReasonByIndustry();

    @Query(value = """
    SELECT
        l.customer_role AS customerRole,

        COUNT(*) AS totalLead

    FROM lead l

    GROUP BY l.customer_role

    ORDER BY totalLead DESC
    """, nativeQuery = true)
    List<TotalLeadByCustomerRoleResponse> getTotalLeadByCustomerRole();

    @Query(value = """
    SELECT
        l.customer_role AS customerRole,

        SUM(
            CASE
                WHEN l.status = 'Won'
                THEN l.business_result
                ELSE 0
            END
        ) AS revenue

    FROM lead l

    GROUP BY l.customer_role

    ORDER BY revenue DESC
    """, nativeQuery = true)
    List<RevenueByCustomerRoleResponse> getRevenueByCustomerRole();

    @Query(value = """
    SELECT
        l.customer_role AS customerRole,

        COUNT(*) AS wonLead

    FROM lead l

    WHERE l.status = 'Won'

    GROUP BY l.customer_role

    ORDER BY wonLead DESC
    """, nativeQuery = true)
    List<WonLeadByCustomerRoleResponse> getWonLeadByCustomerRole();

    @Query(value = """
    SELECT
        l.customer_role AS customerRole,

        COUNT(*) AS totalLead,

        SUM(
            CASE
                WHEN l.status = 'Won' THEN 1
                ELSE 0
            END
        ) AS wonLead,

        ROUND(
            SUM(
                CASE
                    WHEN l.status = 'Won' THEN 1
                    ELSE 0
                END
            ) * 100.0 / COUNT(*),
            2
        ) AS conversionRate

    FROM lead l

    GROUP BY l.customer_role

    ORDER BY conversionRate DESC
    """, nativeQuery = true)
    List<CustomerRoleConversionRateResponse> getCustomerRoleConversionRate();

    @Query(value = """
    SELECT
        l.customer_role AS customerRole,

        COUNT(*) AS wonLead,

        SUM(l.business_result) AS totalRevenue,

        ROUND(
            AVG(CAST(l.business_result AS FLOAT)),
            2
        ) AS avgRevenueWon

    FROM lead l

    WHERE l.status = 'Won'

    GROUP BY l.customer_role

    ORDER BY avgRevenueWon DESC
    """, nativeQuery = true)
    List<AvgRevenueWonByCustomerRoleResponse> getAvgRevenueWonByCustomerRole();

    @Query(value = """
    SELECT
        l.customer_role AS customerRole,

        COUNT(*) AS lostLead

    FROM lead l

    WHERE l.status = 'Lost'

    GROUP BY l.customer_role

    ORDER BY lostLead DESC
    """, nativeQuery = true)
    List<LostLeadByCustomerRoleResponse> getLostLeadByCustomerRole();

    @Query(value = """
    SELECT
        t.customerRole AS customerRole,
        t.lossReason AS lossReason,
        t.lostLead AS lostLead
    FROM (
        SELECT
            l.customer_role AS customerRole,
            l.loss_reason AS lossReason,
            COUNT(*) AS lostLead,

            ROW_NUMBER() OVER (
                PARTITION BY l.customer_role
                ORDER BY COUNT(*) DESC
            ) AS rn

        FROM lead l

        WHERE l.status = 'Lost'
          AND l.loss_reason IS NOT NULL

        GROUP BY l.customer_role, l.loss_reason
    ) t

    WHERE t.rn = 1

    ORDER BY t.lostLead DESC
    """, nativeQuery = true)
    List<BestLostReasonByCustomerRoleResponse> getBestLostReasonByCustomerRole();

    @Query(value = """
    SELECT
        l.customer_role AS customerRole,

        ROUND(
            AVG(
                CAST(
                    DATEDIFF(
                        DAY,
                        l.created_date,
                        CAST(h.changed_at AS DATE)
                    ) AS FLOAT
                )
            ),
            2
        ) AS avgSalesCycle

    FROM lead l

    JOIN lead_status_history h
        ON l.lead_id = h.lead_id

    WHERE h.new_status = 'Won'

    GROUP BY l.customer_role

    ORDER BY avgSalesCycle ASC
    """, nativeQuery = true)
    List<AvgSalesCycleByCustomerRoleResponse> getAvgSalesCycleByCustomerRole();

    @Query(value = """
    SELECT
        l.region AS region,

        COUNT(*) AS totalLead

    FROM lead l

    GROUP BY l.region

    ORDER BY totalLead DESC
    """, nativeQuery = true)
    List<TotalLeadByRegionResponse> getTotalLeadByRegion();

    @Query(value = """
    SELECT
        l.region AS region,

        COUNT(*) AS wonLead

    FROM lead l

    WHERE l.status = 'Won'

    GROUP BY l.region

    ORDER BY wonLead DESC
    """, nativeQuery = true)
    List<WonLeadByRegionResponse> getWonLeadByRegion();

    @Query(value = """
    SELECT
        l.region AS region,

        COUNT(*) AS totalLead,

        SUM(
            CASE
                WHEN l.status = 'Won' THEN 1
                ELSE 0
            END
        ) AS wonLead,

        ROUND(
            SUM(
                CASE
                    WHEN l.status = 'Won' THEN 1
                    ELSE 0
                END
            ) * 100.0 / COUNT(*),
            2
        ) AS conversionRate

    FROM lead l

    GROUP BY l.region

    ORDER BY conversionRate DESC
    """, nativeQuery = true)
    List<RegionConversionRateResponse> getRegionConversionRate();

    @Query(value = """
    SELECT
        l.region AS region,

        COUNT(*) AS wonLead,

        SUM(l.business_result) AS totalRevenue,

        ROUND(
            AVG(CAST(l.business_result AS FLOAT)),
            2
        ) AS avgRevenueWon

    FROM lead l

    WHERE l.status = 'Won'

    GROUP BY l.region

    ORDER BY avgRevenueWon DESC
    """, nativeQuery = true)
    List<AvgRevenueWonByRegionResponse> getAvgRevenueWonByRegion();

    @Query(value = """
    SELECT
        l.region AS region,

        COUNT(*) AS lostLead

    FROM lead l

    WHERE l.status = 'Lost'

    GROUP BY l.region

    ORDER BY lostLead DESC
    """, nativeQuery = true)
    List<LostLeadByRegionResponse> getLostLeadByRegion();

    @Query(value = """
    SELECT
        t.region AS region,
        t.lossReason AS lossReason,
        t.lostLead AS lostLead
    FROM (
        SELECT
            l.region AS region,

            l.loss_reason AS lossReason,

            COUNT(*) AS lostLead,

            ROW_NUMBER() OVER (
                PARTITION BY l.region
                ORDER BY COUNT(*) DESC
            ) AS rn

        FROM lead l

        WHERE l.status = 'Lost'
          AND l.loss_reason IS NOT NULL

        GROUP BY l.region, l.loss_reason
    ) t

    WHERE t.rn = 1

    ORDER BY t.lostLead DESC
    """, nativeQuery = true)
    List<BestLostReasonByRegionResponse> getBestLostReasonByRegion();

    @Query(value = """
    SELECT
        p.product_name AS productName,

        COUNT(DISTINCT l.lead_id) AS totalLead

    FROM lead l

    INNER JOIN lead_item li
        ON l.lead_id = li.lead_id

    INNER JOIN product p
        ON li.product_id = p.product_id

    GROUP BY p.product_name

    ORDER BY totalLead DESC
    """, nativeQuery = true)
    List<TotalLeadByProductLineResponse> getTotalLeadByProductLine();

    @Query(value = """
    SELECT
        p.product_name AS productName,

        COUNT(DISTINCT l.lead_id) AS wonLead

    FROM lead l

    INNER JOIN lead_item li
        ON l.lead_id = li.lead_id

    INNER JOIN product p
        ON li.product_id = p.product_id

    WHERE l.status = 'Won'

    GROUP BY p.product_name

    ORDER BY wonLead DESC
    """, nativeQuery = true)
    List<WonLeadByProductLineResponse> getWonLeadByProductLine();

    @Query(value = """
    SELECT
        p.product_name AS productName,

        COUNT(DISTINCT l.lead_id) AS totalLead,

        COUNT(DISTINCT CASE
            WHEN l.status = 'Won'
            THEN l.lead_id
        END) AS wonLead,

        ROUND(
            COUNT(DISTINCT CASE
                WHEN l.status = 'Won'
                THEN l.lead_id
            END) * 100.0
            /
            COUNT(DISTINCT l.lead_id),
            2
        ) AS conversionRate

    FROM lead l

    INNER JOIN lead_item li
        ON l.lead_id = li.lead_id

    INNER JOIN product p
        ON li.product_id = p.product_id

    GROUP BY p.product_name

    ORDER BY conversionRate DESC
    """, nativeQuery = true)
    List<ProductLineConversionRateResponse> getProductLineConversionRate();

    @Query(value = """
    SELECT
        p.product_name AS productName,

        COUNT(DISTINCT l.lead_id) AS wonLead,

        SUM(l.business_result) AS totalRevenue,

        ROUND(
            AVG(CAST(l.business_result AS FLOAT)),
            2
        ) AS avgRevenueWon

    FROM lead l

    INNER JOIN lead_item li
        ON l.lead_id = li.lead_id

    INNER JOIN product p
        ON li.product_id = p.product_id

    WHERE l.status = 'Won'

    GROUP BY p.product_name

    ORDER BY avgRevenueWon DESC
    """, nativeQuery = true)
    List<AvgRevenueWonByProductLineResponse> getAvgRevenueWonByProductLine();

    @Query(value = """
    SELECT
        p.product_name AS productName,

        COUNT(DISTINCT l.lead_id) AS lostLead

    FROM lead l

    INNER JOIN lead_item li
        ON l.lead_id = li.lead_id

    INNER JOIN product p
        ON li.product_id = p.product_id

    WHERE l.status = 'Lost'

    GROUP BY p.product_name

    ORDER BY lostLead DESC
    """, nativeQuery = true)
    List<LostLeadByProductLineResponse> getLostLeadByProductLine();

    @Query(value = """
    SELECT
        t.productName,
        t.lossReason,
        t.lostLead
    FROM (
        SELECT
            p.product_name AS productName,

            l.loss_reason AS lossReason,

            COUNT(DISTINCT l.lead_id) AS lostLead,

            ROW_NUMBER() OVER (
                PARTITION BY p.product_name
                ORDER BY COUNT(DISTINCT l.lead_id) DESC
            ) AS rn

        FROM lead l

        INNER JOIN lead_item li
            ON l.lead_id = li.lead_id

        INNER JOIN product p
            ON li.product_id = p.product_id

        WHERE l.status = 'Lost'
          AND l.loss_reason IS NOT NULL

        GROUP BY
            p.product_name,
            l.loss_reason
    ) t

    WHERE t.rn = 1

    ORDER BY t.lostLead DESC
    """, nativeQuery = true)
    List<BestLostReasonByProductLineResponse> getBestLostReasonByProductLine();

    @Query(value = """
    SELECT
        l.customer_group AS customerGroup,

        SUM(
            ISNULL(l.business_result, 0)
        ) AS revenue,

        SUM(
            ISNULL(l.cost, 0)
        ) AS cost,

        ROUND(
            CASE
                WHEN SUM(l.cost) = 0 THEN 0
                ELSE
                    (SUM(ISNULL(l.business_result,0)) * 100.0)
                    /
                    SUM(ISNULL(l.cost,0))
            END,
            2
        ) AS roi

    FROM lead l

    GROUP BY l.customer_group

    ORDER BY roi DESC
    """, nativeQuery = true)
    List<CustomerGroupROIResponse> getCustomerGroupROI();

    @Query(value = """
    SELECT
        l.customer_group AS customerGroup,

        COUNT(*) AS totalLead,

        SUM(ISNULL(l.cost, 0)) AS totalCost,

        ROUND(
            CASE
                WHEN COUNT(*) = 0 THEN 0
                ELSE
                    SUM(ISNULL(l.cost, 0)) * 1.0
                    / COUNT(*)
            END,
            2
        ) AS costPerLead

    FROM lead l

    GROUP BY l.customer_group

    ORDER BY costPerLead DESC
    """, nativeQuery = true)
    List<CustomerGroupCPLResponse> getCustomerGroupCostPerLead();



    @Query("SELECT l.status, COUNT(l) FROM LeadEntity l WHERE l.user.email = :email GROUP BY l.status")
    List<Object[]> countLeadByStatusAndSellerEmail(@Param("email") String email);

    @Query(value = """
    SELECT l.lead_id AS leadId, ISNULL(SUM(li.expected_revenue), 0) AS expectedRevenue
    FROM lead l
    LEFT JOIN lead_item li ON l.lead_id = li.lead_id
    GROUP BY l.lead_id
    """, nativeQuery = true)
    List<LeadRevenueProjection> getLeadsExpectedRevenue();

    @Query(value = """
    SELECT TOP (10)

        l.account AS account,

        l.industry_type AS industry,

        STRING_AGG(DISTINCT p.product_name, ', ') AS productLine,

        l.customer_group AS customerGroup,

        l.region AS region,

        l.customer_role AS customerRole,

        ROUND(
            SUM(ISNULL(l.cost,0)) * 1.0
            /
            COUNT(DISTINCT l.lead_id),
            2
        ) AS costPerLead,

        SUM(ISNULL(l.business_result,0)) AS revenue

    FROM lead l

    LEFT JOIN lead_item li
        ON l.lead_id = li.lead_id

    LEFT JOIN product p
        ON li.product_id = p.product_id

    WHERE l.status = 'Won'

    GROUP BY
        l.account,
        l.industry_type,
        l.customer_group,
        l.region,
        l.customer_role

    ORDER BY revenue DESC
    """, nativeQuery = true)
    List<Top10AccountRevenueResponse> getTop10Accounts();

    @Query(value = """
    SELECT TOP (1)

        u.user_code AS userCode,

        u.full_name AS fullName,

        COUNT(l.lead_id) AS wonDeal,

        SUM(ISNULL(l.business_result, 0)) AS revenue

    FROM lead l

    INNER JOIN [user] u
        ON l.user_id = u.user_id

    WHERE l.status = 'Won'

    GROUP BY
        u.user_code,
        u.full_name

    ORDER BY revenue DESC
    """, nativeQuery = true)
    TopSalesOwnerRevenueResponse getTopSalesOwnerRevenue();

    @Query(value = """
    SELECT TOP (1)

        u.user_code AS userCode,

        u.full_name AS fullName,

        COUNT(l.lead_id) AS totalLead,

        SUM(
            CASE
                WHEN l.status = 'Won' THEN 1
                ELSE 0
            END
        ) AS wonLead,

        ROUND(
            SUM(
                CASE
                    WHEN l.status = 'Won' THEN 1
                    ELSE 0
                END
            ) * 100.0
            /
            COUNT(l.lead_id),
            2
        ) AS winRate

    FROM lead l

    INNER JOIN [user] u
        ON l.user_id = u.user_id

    GROUP BY
        u.user_code,
        u.full_name

    HAVING COUNT(l.lead_id) > 0

    ORDER BY winRate DESC,
             wonLead DESC
    """, nativeQuery = true)
    TopSalesOwnerWinRateResponse getTopSalesOwnerWinRate();

    @Query(value = """
    SELECT TOP (1)

        u.user_code AS userCode,

        u.full_name AS fullName,

        COUNT(DISTINCT l.lead_id) AS wonDeal,

        ROUND(
            AVG(
                CAST(
                    DATEDIFF(
                        DAY,
                        l.created_date,
                        h.wonDate
                    ) AS FLOAT
                )
            ),
            2
        ) AS avgSalesCycle

    FROM lead l

    INNER JOIN [user] u
        ON l.user_id = u.user_id

    INNER JOIN (

        SELECT
            lead_id,
            MIN(CAST(changed_at AS DATE)) AS wonDate

        FROM lead_status_history

        WHERE new_status = 'Won'

        GROUP BY lead_id

    ) h
        ON l.lead_id = h.lead_id

    WHERE l.status = 'Won'

    GROUP BY
        u.user_code,
        u.full_name

    HAVING COUNT(DISTINCT l.lead_id) > 0

    ORDER BY avgSalesCycle ASC,
             wonDeal DESC
    """, nativeQuery = true)
    FastestSalesOwnerResponse getFastestSalesOwner();

    @Query(value = """
    SELECT

        u.user_code AS userCode,

        u.full_name AS fullName,

        ROUND(
            AVG(
                CAST(
                    DATEDIFF(
                        DAY,
                        l.created_date,
                        h.wonDate
                    ) AS FLOAT
                )
            ),
            2
        ) AS avgSalesCycle

    FROM lead l

    INNER JOIN [user] u
        ON l.user_id = u.user_id

    INNER JOIN (

        SELECT
            lead_id,
            MIN(CAST(changed_at AS DATE)) AS wonDate

        FROM lead_status_history

        WHERE new_status = 'Won'

        GROUP BY lead_id

    ) h
        ON l.lead_id = h.lead_id

    WHERE l.status = 'Won'

    GROUP BY
        u.user_code,
        u.full_name

    ORDER BY avgSalesCycle ASC
    """, nativeQuery = true)
    List<SalesOwnerAvgSalesCycleResponse> getSalesOwnerAvgSalesCycle();

    @Query(value = """
    SELECT

        u.user_code AS userCode,

        u.full_name AS fullName,

        COUNT(DISTINCT l.lead_id) AS totalLead,

        COUNT(DISTINCT CASE
            WHEN b.budget > 0
             AND b.authority > 0
             AND b.need > 0
             AND b.timeline > 0
            THEN l.lead_id
        END) AS completeLead,

        ROUND(

            COUNT(DISTINCT CASE
                WHEN b.budget > 0
                 AND b.authority > 0
                 AND b.need > 0
                 AND b.timeline > 0
                THEN l.lead_id
            END)

            *100.0

            /

            COUNT(DISTINCT l.lead_id)

        ,2) AS bantCompleteRate

    FROM lead l

    INNER JOIN [user] u
        ON l.user_id = u.user_id

    LEFT JOIN lead_bant_point b
        ON l.lead_id = b.lead_id

    GROUP BY
        u.user_code,
        u.full_name

    ORDER BY bantCompleteRate DESC
    """, nativeQuery = true)
    List<SalesOwnerBantCompleteRateResponse> getSalesOwnerBantCompleteRate();

    @Query(value = """
    SELECT

        u.user_code AS userCode,

        u.full_name AS fullName,

        ROUND(
            AVG(
                CAST(b.total_score AS FLOAT)
            ),
            2
        ) AS avgBantScore

    FROM lead l

    INNER JOIN [user] u
        ON l.user_id = u.user_id

    INNER JOIN lead_bant_point b
        ON l.lead_id = b.lead_id

    GROUP BY
        u.user_code,
        u.full_name

    ORDER BY avgBantScore DESC
    """, nativeQuery = true)
    List<SalesOwnerAvgBantScoreResponse> getSalesOwnerAvgBantScore();

    @Query(value = """
    SELECT

        p.product_name AS productName,

        l.loss_reason AS lossReason,

        COUNT(DISTINCT l.lead_id) AS lostLead

    FROM lead l

    INNER JOIN lead_item li
        ON l.lead_id = li.lead_id

    INNER JOIN product p
        ON li.product_id = p.product_id

    WHERE l.status = 'Lost'
      AND l.loss_reason IS NOT NULL

    GROUP BY
        p.product_name,
        l.loss_reason

    ORDER BY
        p.product_name,
        lostLead DESC
    """, nativeQuery = true)
    List<LossReasonByProductLineResponse> getLossReasonByProductLine();

    @Query(value = """
    SELECT

        COUNT(DISTINCT CASE
            WHEN l.status IN ('Qualified','Proposal','Negotiation','Won')
            THEN l.lead_id
        END) AS qualifiedLeads,

        COUNT(DISTINCT CASE
            WHEN l.status = 'Won'
            THEN l.lead_id
        END) AS wonLeads,

        COUNT(DISTINCT CASE
            WHEN l.status = 'Lost'
            THEN l.lead_id
        END) AS lostLeads,

        ROUND(
            COUNT(DISTINCT CASE
                WHEN l.status = 'Won'
                THEN l.lead_id
            END) * 100.0
            /
            COUNT(DISTINCT l.lead_id)
        ,2) AS winRate,

        ROUND(
            AVG(
                CASE
                    WHEN l.status = 'Won'
                    THEN CAST(l.business_result AS FLOAT)
                END
            )
        ,2) AS avgDealSize,

        ROUND(
            AVG(
                CASE
                    WHEN l.status = 'Won'
                    THEN CAST(
                        DATEDIFF(
                            DAY,
                            l.created_date,
                            h.wonDate
                        ) AS FLOAT
                    )
                END
            )
        ,2) AS avgSalesCycle

    FROM lead l

    INNER JOIN [user] u
        ON l.user_id = u.user_id

    LEFT JOIN (

        SELECT
            lead_id,
            MIN(CAST(changed_at AS DATE)) AS wonDate

        FROM lead_status_history

        WHERE new_status = 'Won'

        GROUP BY lead_id

    ) h
        ON l.lead_id = h.lead_id

    WHERE u.user_code = :userCode
    """, nativeQuery = true)
    SalesOwnerDetailResponse getSalesOwnerDetail(
            @Param("userCode") String userCode
    );

    @Query(value = """
        SELECT 
            COUNT(l.lead_id) as totalLeads,
            CAST(SUM(CASE WHEN l.status = 'Won' THEN 1 ELSE 0 END) AS BIGINT) as wonLeads,
            CAST(SUM(CASE WHEN l.status = 'Lost' THEN 1 ELSE 0 END) AS BIGINT) as lostLeads
        FROM lead l
        WHERE l.user_id = :userId
          AND l.created_date >= :sinceDate
        """, nativeQuery = true)
    PerformanceStatsProjection getPerformanceStatsForUser(
        @Param("userId") java.util.UUID userId,
        @Param("sinceDate") java.time.LocalDate sinceDate
    );

}