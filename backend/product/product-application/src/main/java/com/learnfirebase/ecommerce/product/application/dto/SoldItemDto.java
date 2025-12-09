package com.learnfirebase.ecommerce.product.application.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SoldItemDto {
    String productId;
    int quantity;
}
