package com.learnfirebase.ecommerce.cart.application.command;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MergeCartCommand {
    String cartId;
    @Singular
    List<MergeItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MergeItem {
        String productId;
        String variantSku;
        int quantity;
        String price;
        String currency;
    }
}
