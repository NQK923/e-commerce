package com.learnfirebase.ecommerce.logistics.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateShipmentCommand {
    String orderId;
    String shippingMethodId;
    String destination;
}
