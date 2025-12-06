package com.learnfirebase.ecommerce.product.application.command;

import com.learnfirebase.ecommerce.product.domain.model.ReportReason;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateProductReportCommand {
    private String productId;
    private String userId;
    private ReportReason reason;
    private String description;
}
