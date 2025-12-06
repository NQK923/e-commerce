package com.learnfirebase.ecommerce.product.domain.model;

import java.time.Instant;

import com.learnfirebase.ecommerce.common.domain.AggregateRoot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReport extends AggregateRoot<ProductReportId> {
    private ProductReportId id;
    private ProductId productId;
    // We might store userId as String since User domain is separate
    private String userId; 
    private ReportReason reason;
    private String description;
    private ReportStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public void resolve() {
        this.status = ReportStatus.RESOLVED;
        this.updatedAt = Instant.now();
    }

    public void reject() {
        this.status = ReportStatus.REJECTED;
        this.updatedAt = Instant.now();
    }
}
