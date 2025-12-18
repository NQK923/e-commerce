package com.learnfirebase.ecommerce.product.application.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductReviewDto {
    private String id;
    private String productId;
    private String userId;
    private String userName;
    private Integer rating;
    private String comment;
    private boolean verifiedPurchase;
    private int abuseReportCount;
    private String sellerResponse;
    private Instant sellerRespondedAt;
    private Instant updatedAt;
    private Instant createdAt;
}
