package com.learnfirebase.ecommerce.product.infrastructure.persistence;

import com.learnfirebase.ecommerce.product.application.command.ReportReviewCommand;
import com.learnfirebase.ecommerce.product.application.port.out.ProductReviewReportRepositoryPort;
import com.learnfirebase.ecommerce.product.domain.model.ReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductReviewReportRepositoryAdapter implements ProductReviewReportRepositoryPort {
    private final ProductReviewReportJpaRepository jpaRepository;

    @Transactional
    @Override
    public void saveReport(ReportReviewCommand command) {
        Instant now = Instant.now();
        ProductReviewReportEntity entity = ProductReviewReportEntity.builder()
                .id(UUID.randomUUID().toString())
                .reviewId(command.getReviewId())
                .reporterUserId(command.getReporterUserId())
                .reason(command.getReason())
                .description(command.getDescription())
                .status(ReportStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
        jpaRepository.save(Objects.requireNonNull(entity));
    }

    @Override
    public boolean hasReported(String reviewId, String reporterUserId) {
        return jpaRepository.existsByReviewIdAndReporterUserId(Objects.requireNonNull(reviewId),
                Objects.requireNonNull(reporterUserId));
    }
}
