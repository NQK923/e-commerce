package com.learnfirebase.ecommerce.cart.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoveItemCommand {
    String cartId;
    String productId;
    String variantSku;
}
