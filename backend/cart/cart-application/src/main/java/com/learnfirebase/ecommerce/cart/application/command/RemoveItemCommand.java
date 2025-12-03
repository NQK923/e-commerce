package com.learnfirebase.ecommerce.cart.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RemoveItemCommand {
    String cartId;
    String productId;
}
