package com.learnfirebase.ecommerce.product.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface ProductReviewJpaRepository extends JpaRepository<ProductReviewEntity, String> {
    Page<ProductReviewEntity> findByProductIdOrderByCreatedAtDesc(String productId, Pageable pageable);
    boolean existsByProductIdAndUserId(String productId, String userId);
    long countByUserIdAndCreatedAtAfter(String userId, Instant createdAfter);
}
