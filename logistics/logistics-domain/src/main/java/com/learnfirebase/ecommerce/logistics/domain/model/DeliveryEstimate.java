package com.learnfirebase.ecommerce.logistics.domain.model;

import java.time.Instant;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DeliveryEstimate {
    Instant estimatedDelivery;
    String notes;
}
