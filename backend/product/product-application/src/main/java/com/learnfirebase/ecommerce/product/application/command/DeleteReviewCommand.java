package com.learnfirebase.ecommerce.product.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteReviewCommand {
    private String reviewId;
    private String productId;
    private String userId;
}
