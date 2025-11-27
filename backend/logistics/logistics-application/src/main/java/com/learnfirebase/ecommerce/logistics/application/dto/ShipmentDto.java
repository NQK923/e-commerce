package com.learnfirebase.ecommerce.logistics.application.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShipmentDto {
    String id;
    String orderId;
    String methodId;
    String cost;
    String currency;
    Instant shippedAt;
}
