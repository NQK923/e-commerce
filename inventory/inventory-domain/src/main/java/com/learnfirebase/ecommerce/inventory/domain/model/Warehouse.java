package com.learnfirebase.ecommerce.inventory.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Warehouse {
    String id;
    String name;
}
