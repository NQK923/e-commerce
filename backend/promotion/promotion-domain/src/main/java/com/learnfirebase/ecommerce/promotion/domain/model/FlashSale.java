package com.learnfirebase.ecommerce.promotion.domain.model;

import java.time.Instant;

import com.learnfirebase.ecommerce.common.domain.AggregateRoot;
import com.learnfirebase.ecommerce.common.domain.valueobject.Money;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class FlashSale extends AggregateRoot<FlashSaleId> {
    private FlashSaleId id;
    private String productId;
    private Money price;
    private Money originalPrice;
    private Instant startTime;
    private Instant endTime;
    private Integer totalQuantity;
    private Integer remainingQuantity;
    private FlashSaleStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public boolean isActive() {
        Instant now = Instant.now();
        return status == FlashSaleStatus.ACTIVE &&
               now.isAfter(startTime) &&
               now.isBefore(endTime) &&
               remainingQuantity > 0;
    }

    public void decrementStock(int quantity) {
        if (remainingQuantity < quantity) {
            throw new IllegalStateException("Not enough stock in flash sale");
        }
        this.remainingQuantity -= quantity;
    }
}
