package com.learnfirebase.ecommerce.cart.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ClearCartCommand {
    String cartId;
}
