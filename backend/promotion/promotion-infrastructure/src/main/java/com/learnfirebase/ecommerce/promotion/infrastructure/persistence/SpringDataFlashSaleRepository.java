package com.learnfirebase.ecommerce.promotion.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataFlashSaleRepository extends JpaRepository<FlashSaleEntity, UUID> {
    
    @Query("SELECT f FROM FlashSaleEntity f WHERE f.productId = :productId AND f.status = 'ACTIVE' AND f.startTime <= CURRENT_TIMESTAMP AND f.endTime >= CURRENT_TIMESTAMP")
    Optional<FlashSaleEntity> findActiveByProductId(String productId);

    @Query("SELECT f FROM FlashSaleEntity f WHERE f.status = 'ACTIVE' AND f.startTime <= CURRENT_TIMESTAMP AND f.endTime >= CURRENT_TIMESTAMP")
    java.util.List<FlashSaleEntity> findAllActive();
}
