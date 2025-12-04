package com.learnfirebase.ecommerce.identity.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnfirebase.ecommerce.identity.domain.model.SellerApplicationStatus;

public interface SellerApplicationJpaRepository extends JpaRepository<SellerApplicationEntity, String> {
    Optional<SellerApplicationEntity> findTopByUserIdOrderByCreatedAtDesc(String userId);
    List<SellerApplicationEntity> findByStatusOrderByCreatedAtDesc(SellerApplicationStatus status);
}
