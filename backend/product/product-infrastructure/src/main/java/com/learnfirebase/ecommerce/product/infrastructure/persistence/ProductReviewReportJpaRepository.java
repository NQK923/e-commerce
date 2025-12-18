package com.learnfirebase.ecommerce.product.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductReviewReportJpaRepository extends JpaRepository<ProductReviewReportEntity, String> {
    boolean existsByReviewIdAndReporterUserId(String reviewId, String reporterUserId);
}
