package com.learnfirebase.ecommerce.cart.application.command;

import java.util.List;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class MergeCartCommand {
    String cartId;
    @Singular
    List<MergeItem> items;

    @Value
    @Builder
    public static class MergeItem {
        String productId;
        int quantity;
        String price;
        String currency;
    }
}
