package com.helu.internship.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lead")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadEntity {

    @Id
    @Column(name = "lead_id", length = 50)
    private String leadId;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "account", length = 150, nullable = false)
    private String account;

    @Column(name = "industry_type", length = 100, nullable = false)
    private String industryType;

    @Column(name = "customer_group", length = 50, nullable = false)
    private String customerGroup;

    @Column(name = "customer_role", length = 50, nullable = false)
    private String customerRole;

    @Column(name = "location", length = 100, nullable = false)
    private String location;

    @Column(name = "region", length = 50, nullable = false)
    private String region;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "cost", nullable = false)
    private BigDecimal cost;

    @Column(name = "loss_reason", length = 100)
    private String lossReason;

    @Column(name = "business_result")
    private BigDecimal businessResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    private LeadSourceEntity source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
