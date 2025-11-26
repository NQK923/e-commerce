package com.learnfirebase.ecommerce.logistics.domain.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Shipment {
    private String id;
    private String orderId;
    private ShippingMethod method;
    private Instant shippedAt;
    private DeliveryEstimate estimate;
}
