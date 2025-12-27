package com.learnfirebase.ecommerce.promotion.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.learnfirebase.ecommerce.promotion.domain.model.DiscountType;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coupons")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponEntity {
    @Id
    private String id;
    
    @Column(unique = true, nullable = false)
    private String code;
    
    private String sellerId;
    
    @ElementCollection
    @CollectionTable(name = "coupon_products", joinColumns = @JoinColumn(name = "coupon_id"))
    @Column(name = "product_id")
    private List<String> applicableProductIds;
    
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;
    
    private BigDecimal discountValue;
    
    private BigDecimal minOrderAmount;
    private String minOrderCurrency;
    
    private BigDecimal maxDiscountAmount;
    private String maxDiscountCurrency;
    
    private int usageLimit;
    private int usedCount;
    
    private Instant startAt;
    private Instant endAt;
    
    private Instant createdAt;
    private Instant updatedAt;
}
