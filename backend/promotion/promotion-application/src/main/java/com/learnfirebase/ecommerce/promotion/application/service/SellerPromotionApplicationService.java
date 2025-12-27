package com.learnfirebase.ecommerce.promotion.application.service;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

import com.learnfirebase.ecommerce.common.domain.valueobject.Money;
import com.learnfirebase.ecommerce.promotion.application.command.CreateCouponCommand;
import com.learnfirebase.ecommerce.promotion.application.port.out.CouponRepository;
import com.learnfirebase.ecommerce.promotion.domain.exception.PromotionDomainException;
import com.learnfirebase.ecommerce.promotion.domain.model.Coupon;
import com.learnfirebase.ecommerce.promotion.domain.model.CouponId;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerPromotionApplicationService {
    private final CouponRepository couponRepository;

    @Transactional
    public Coupon createCoupon(CreateCouponCommand command) {
        if (couponRepository.findByCode(command.getCode()).isPresent()) {
            throw new PromotionDomainException("Coupon code already exists");
        }

        Money minOrder = command.getMinOrderAmount() != null 
            ? Money.builder().amount(command.getMinOrderAmount()).currency(command.getCurrency()).build() 
            : null;
            
        Money maxDiscount = command.getMaxDiscountAmount() != null 
            ? Money.builder().amount(command.getMaxDiscountAmount()).currency(command.getCurrency()).build() 
            : null;

        Coupon coupon = Coupon.builder()
            .id(new CouponId(UUID.randomUUID().toString()))
            .code(command.getCode())
            .sellerId(command.getSellerId())
            .applicableProductIds(command.getApplicableProductIds())
            .discountType(command.getDiscountType())
            .discountValue(command.getDiscountValue())
            .minOrderValue(minOrder)
            .maxDiscountAmount(maxDiscount)
            .usageLimit(command.getUsageLimit())
            .usedCount(0)
            .startAt(command.getStartAt())
            .endAt(command.getEndAt())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        return couponRepository.save(coupon);
    }

    public List<Coupon> getSellerCoupons(String sellerId) {
        return couponRepository.findBySellerId(sellerId);
    }
}
