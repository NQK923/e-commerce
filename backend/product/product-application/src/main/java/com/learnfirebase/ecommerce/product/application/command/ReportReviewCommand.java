package com.learnfirebase.ecommerce.product.application.command;

import com.learnfirebase.ecommerce.product.domain.model.ReportReason;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportReviewCommand {
    private String reviewId;
    private String productId;
    private String reporterUserId;
    private ReportReason reason;
    private String description;
}
