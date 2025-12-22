package com.learnfirebase.ecommerce.product.infrastructure.persistence;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewEntity {
    @Id
    private String id;
    private String productId;
    private String userId;
    private String userName;
    private Integer rating;
    private String comment;
    private boolean verifiedPurchase;
    private int abuseReportCount;
    private String sellerResponse;
    @Column(name = "seller_response_at")
    private Instant sellerRespondedAt;
    private String sellerId;
    private Instant createdAt;
    private Instant updatedAt;
}
