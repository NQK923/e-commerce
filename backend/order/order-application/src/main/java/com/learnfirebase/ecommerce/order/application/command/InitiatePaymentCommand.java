package com.learnfirebase.ecommerce.order.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InitiatePaymentCommand {
    String orderId;
    String returnUrl;
    String clientIp;
}
