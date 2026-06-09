package com.helu.internship.repo;

import com.helu.internship.dto.response.LeadListProjection;
import com.helu.internship.entity.LeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
