package com.learnfirebase.ecommerce.product.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewCommand {
    private String productId;
    private String userId;
    private String userName;
    private Integer rating;
    private String comment;
}
