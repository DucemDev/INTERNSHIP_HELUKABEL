package com.helu.internship.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity {

    @Id
    @Column(name = "product_id", length = 50)
    private String productId;

    @Column(name = "product_name", length = 100, nullable = false)
    private String productName;
}