package com.learnfirebase.ecommerce.promotion.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.learnfirebase.ecommerce.common.domain.AggregateRoot;
import com.learnfirebase.ecommerce.common.domain.valueobject.Money;
import com.learnfirebase.ecommerce.promotion.domain.exception.PromotionDomainException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon extends AggregateRoot<CouponId> {
    private CouponId id;
    private String code;
    private String sellerId;
    private List<String> applicableProductIds;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private Money minOrderValue;
    private Money maxDiscountAmount;
    private int usageLimit;
    private int usedCount;
    private Instant startAt;
    private Instant endAt;
    private Instant createdAt;
    private Instant updatedAt;

    public void incrementUsage() {
        if (usageLimit > 0 && usedCount >= usageLimit) {
            throw new PromotionDomainException("Coupon usage limit exceeded");
        }
        this.usedCount++;
        this.updatedAt = Instant.now();
    }

    public boolean isValid() {
        Instant now = Instant.now();
        return (startAt == null || !now.isBefore(startAt)) &&
               (endAt == null || !now.isAfter(endAt)) &&
               (usageLimit <= 0 || usedCount < usageLimit);
    }
}
