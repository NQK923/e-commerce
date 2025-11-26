package com.learnfirebase.ecommerce.product.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Category {
    String id;
    String name;
}
