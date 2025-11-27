package com.learnfirebase.ecommerce.order.domain.model;

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
@EqualsAndHashCode(of = {"productId"})
public class OrderItem {
    private String productId;
    private int quantity;
    private Money price;

    public Money subTotal() {
        return price.multiply(quantity);
    }
}
