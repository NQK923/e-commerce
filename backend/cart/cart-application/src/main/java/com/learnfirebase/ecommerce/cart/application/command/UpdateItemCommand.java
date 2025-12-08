package com.learnfirebase.ecommerce.cart.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemCommand {
    String cartId;
    String productId;
    int quantity;
    String price;
    String currency;
}
