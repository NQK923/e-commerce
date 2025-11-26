package com.learnfirebase.ecommerce.logistics.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingRateJpaRepository extends JpaRepository<ShippingRateEntity, String> {
    Optional<ShippingRateEntity> findByMethodIdAndDestination(String methodId, String destination);
}
