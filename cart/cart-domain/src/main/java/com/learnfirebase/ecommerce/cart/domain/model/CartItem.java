package com.learnfirebase.ecommerce.cart.domain.model;

import com.learnfirebase.ecommerce.common.domain.valueobject.Money;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String productId;
    private int quantity;
    private Money price;
}
