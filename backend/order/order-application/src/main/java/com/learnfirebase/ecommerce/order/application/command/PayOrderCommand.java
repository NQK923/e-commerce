package com.learnfirebase.ecommerce.order.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PayOrderCommand {
    String orderId;
    String paymentReference;
}
