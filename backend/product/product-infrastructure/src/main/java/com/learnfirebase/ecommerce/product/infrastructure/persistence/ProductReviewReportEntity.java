package com.learnfirebase.ecommerce.product.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.learnfirebase.ecommerce.product.application.command.ReportReviewCommand;
import com.learnfirebase.ecommerce.product.application.port.out.ProductReviewReportRepositoryPort;
import com.learnfirebase.ecommerce.product.domain.model.ReportReason;
import com.learnfirebase.ecommerce.product.domain.model.ReportStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "product_review_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewReportEntity {
    @Id
    private String id;
    private String reviewId;
    private String reporterUserId;

    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    private String description;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    private Instant createdAt;
    private Instant updatedAt;
}

