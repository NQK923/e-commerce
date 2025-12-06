package com.learnfirebase.ecommerce.product.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.learnfirebase.ecommerce.product.domain.model.ProductReport;
import com.learnfirebase.ecommerce.product.domain.model.ProductReportId;
import com.learnfirebase.ecommerce.product.domain.model.ProductId;
import com.learnfirebase.ecommerce.product.domain.model.ReportReason;
import com.learnfirebase.ecommerce.product.domain.model.ReportStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_reports")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReportEntity {
    @Id
    private String id;
    private String productId;
    private String userId;
    
    @Enumerated(EnumType.STRING)
    private ReportReason reason;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    private ReportStatus status;
    
    private Instant createdAt;
    private Instant updatedAt;

    public ProductReport toDomain() {
        return ProductReport.builder()
                .id(new ProductReportId(id))
                .productId(new ProductId(productId))
                .userId(userId)
                .reason(reason)
                .description(description)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static ProductReportEntity fromDomain(ProductReport report) {
        return ProductReportEntity.builder()
                .id(report.getId().getValue())
                .productId(report.getProductId().getValue())
                .userId(report.getUserId())
                .reason(report.getReason())
                .description(report.getDescription())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
