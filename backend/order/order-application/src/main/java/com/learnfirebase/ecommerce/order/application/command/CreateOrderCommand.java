package com.learnfirebase.ecommerce.order.application.command;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateOrderCommand {
    String userId;
    List<OrderItemCommand> items;
    String currency;
    String address;
    String paymentMethod;

    @Value
    @Builder
    public static class OrderItemCommand {
        String productId;
        String variantSku;
        String flashSaleId;
        int quantity;
        String price;
    }
}
