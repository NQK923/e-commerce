package com.learnfirebase.ecommerce.promotion.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionJpaRepository extends JpaRepository<PromotionEntity, String> {
    Optional<PromotionEntity> findByCode(String code);
}
