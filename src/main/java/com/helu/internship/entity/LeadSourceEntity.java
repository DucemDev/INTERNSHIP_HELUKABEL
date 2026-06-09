package com.helu.internship.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lead_source")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadSourceEntity {

    @Id
    @Column(name = "source_id", length = 50)
    private String sourceId;

    @Column(name = "source_name", length = 100, nullable = false)
    private String sourceName;
}