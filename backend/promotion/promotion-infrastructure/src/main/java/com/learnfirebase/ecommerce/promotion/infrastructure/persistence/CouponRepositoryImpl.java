package com.learnfirebase.ecommerce.promotion.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.common.domain.valueobject.Money;
import com.learnfirebase.ecommerce.promotion.application.port.out.CouponRepository;
import com.learnfirebase.ecommerce.promotion.domain.model.Coupon;
import com.learnfirebase.ecommerce.promotion.domain.model.CouponId;
import com.learnfirebase.ecommerce.promotion.infrastructure.persistence.entity.CouponEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {
    private final CouponJpaRepository jpaRepository;

    @Override
    public Coupon save(Coupon coupon) {
        return toDomain(jpaRepository.save(toEntity(coupon)));
    }

    @Override
    public Optional<Coupon> findById(CouponId id) {
        return jpaRepository.findById(id.getValue()).map(this::toDomain);
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return jpaRepository.findByCode(code).map(this::toDomain);
    }

    @Override
    public List<Coupon> findBySellerId(String sellerId) {
        return jpaRepository.findBySellerId(sellerId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private CouponEntity toEntity(Coupon domain) {
        return CouponEntity.builder()
            .id(domain.getId().getValue())
            .code(domain.getCode())
            .sellerId(domain.getSellerId())
            .applicableProductIds(domain.getApplicableProductIds())
            .discountType(domain.getDiscountType())
            .discountValue(domain.getDiscountValue())
            .minOrderAmount(domain.getMinOrderValue() != null ? domain.getMinOrderValue().getAmount() : null)
            .minOrderCurrency(domain.getMinOrderValue() != null ? domain.getMinOrderValue().getCurrency() : null)
            .maxDiscountAmount(domain.getMaxDiscountAmount() != null ? domain.getMaxDiscountAmount().getAmount() : null)
            .maxDiscountCurrency(domain.getMaxDiscountAmount() != null ? domain.getMaxDiscountAmount().getCurrency() : null)
            .usageLimit(domain.getUsageLimit())
            .usedCount(domain.getUsedCount())
            .startAt(domain.getStartAt())
            .endAt(domain.getEndAt())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
    }

    private Coupon toDomain(CouponEntity entity) {
        return Coupon.builder()
            .id(new CouponId(entity.getId()))
            .code(entity.getCode())
            .sellerId(entity.getSellerId())
            .applicableProductIds(entity.getApplicableProductIds())
            .discountType(entity.getDiscountType())
            .discountValue(entity.getDiscountValue())
            .minOrderValue(entity.getMinOrderAmount() != null 
                ? Money.builder().amount(entity.getMinOrderAmount()).currency(entity.getMinOrderCurrency()).build() 
                : null)
            .maxDiscountAmount(entity.getMaxDiscountAmount() != null 
                ? Money.builder().amount(entity.getMaxDiscountAmount()).currency(entity.getMaxDiscountCurrency()).build() 
                : null)
            .usageLimit(entity.getUsageLimit())
            .usedCount(entity.getUsedCount())
            .startAt(entity.getStartAt())
            .endAt(entity.getEndAt())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
