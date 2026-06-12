package com.learnfirebase.ecommerce.promotion.infrastructure.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "promotion_usages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionUsageEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String promotionCode;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Instant usedAt;
}
