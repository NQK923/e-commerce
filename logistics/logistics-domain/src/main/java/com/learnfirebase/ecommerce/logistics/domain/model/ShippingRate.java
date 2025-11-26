package com.learnfirebase.ecommerce.logistics.domain.model;

import com.learnfirebase.ecommerce.common.domain.valueobject.Money;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShippingRate {
    String methodId;
    String destination;
    Money cost;
}
