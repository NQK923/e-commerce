package com.learnfirebase.ecommerce.product.application.dto;

import java.time.Instant;
import com.learnfirebase.ecommerce.product.domain.model.ReportReason;
import com.learnfirebase.ecommerce.product.domain.model.ReportStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductReportDto {
    private String id;
    private String productId;
    private String userId;
    private ReportReason reason;
    private String description;
    private ReportStatus status;
    private Instant createdAt;
}
