package com.learnfirebase.ecommerce.product.domain.model;

import com.learnfirebase.ecommerce.common.domain.valueobject.Money;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductVariant {
    String sku;
    String name;
    Money price;
}
