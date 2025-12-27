package com.learnfirebase.ecommerce.promotion.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnfirebase.ecommerce.promotion.infrastructure.persistence.entity.CouponEntity;

public interface CouponJpaRepository extends JpaRepository<CouponEntity, String> {
    Optional<CouponEntity> findByCode(String code);
    List<CouponEntity> findBySellerId(String sellerId);
}
