package com.learnfirebase.ecommerce.cart.application.dto;

import java.util.List;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class CartDto {
    String id;
    @Singular
    List<CartItemDto> items;
    String total;
    String currency;
    String subtotal;
    String discountTotal;
    String shippingEstimate;

    @Value
    @Builder
    public static class CartItemDto {
        String productId;
        String variantSku;
        int quantity;
        String price;
        String currency;
    }
}
