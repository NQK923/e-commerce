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
    private String variantSku;
    private int quantity;
    private Money price;

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(Money price) {
        this.price = price;
    }
}
