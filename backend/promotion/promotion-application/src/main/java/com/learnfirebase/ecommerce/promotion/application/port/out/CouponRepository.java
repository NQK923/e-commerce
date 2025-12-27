package com.learnfirebase.ecommerce.promotion.application.port.out;

import java.util.List;
import java.util.Optional;

import com.learnfirebase.ecommerce.promotion.domain.model.Coupon;
import com.learnfirebase.ecommerce.promotion.domain.model.CouponId;

public interface CouponRepository {
    Coupon save(Coupon coupon);
    Optional<Coupon> findById(CouponId id);
    Optional<Coupon> findByCode(String code);
    List<Coupon> findBySellerId(String sellerId);
}
