package com.learnfirebase.ecommerce.cart.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AddItemCommand {
    String cartId;
    String productId;
    int quantity;
    String price;
    String currency;
}
