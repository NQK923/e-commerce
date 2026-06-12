package com.learnfirebase.ecommerce.promotion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionUsageJpaRepository extends JpaRepository<PromotionUsageEntity, String> {
}
