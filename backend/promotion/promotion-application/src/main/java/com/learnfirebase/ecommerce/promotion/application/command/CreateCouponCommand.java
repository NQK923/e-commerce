package com.learnfirebase.ecommerce.promotion.application.command;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.learnfirebase.ecommerce.promotion.domain.model.DiscountType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateCouponCommand {
    private String code;
    private String sellerId;
    private List<String> applicableProductIds;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private String currency;
    private BigDecimal maxDiscountAmount;
    private int usageLimit;
    private Instant startAt;
    private Instant endAt;
}
