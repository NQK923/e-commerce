package com.learnfirebase.ecommerce.logistics.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShippingMethod {
    String id;
    String name;
}
