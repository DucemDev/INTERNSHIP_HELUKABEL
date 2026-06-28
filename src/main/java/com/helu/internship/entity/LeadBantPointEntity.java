package com.helu.internship.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lead_bant_point")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadBantPointEntity {

    @Id
    @Column(name = "lead_id", length = 50)
    private String leadId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "lead_id")
    private LeadEntity lead;

    @Column(name = "budget", nullable = false)
    private Integer budget;

    @Column(name = "authority", nullable = false)
    private Integer authority;

    @Column(name = "need", nullable = false)
    private Integer need;

    @Column(name = "timeline", nullable = false)
    private Integer timeline;

    @Column(name = "total_score", insertable = false, updatable = false)
    private Integer totalScore;
}
