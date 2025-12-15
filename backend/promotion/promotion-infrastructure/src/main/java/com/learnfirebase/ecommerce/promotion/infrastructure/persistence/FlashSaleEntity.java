package com.learnfirebase.ecommerce.promotion.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.learnfirebase.ecommerce.promotion.domain.model.FlashSaleStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "flash_sales")
public class FlashSaleEntity {
    @Id
    private UUID id;
    private String productId;
    private BigDecimal price;
    private String currency;
    private BigDecimal originalPrice;
    private String originalCurrency;
    private Instant startTime;
    private Instant endTime;
    private Integer totalQuantity;
    private Integer remainingQuantity;
    @Enumerated(EnumType.STRING)
    private FlashSaleStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
