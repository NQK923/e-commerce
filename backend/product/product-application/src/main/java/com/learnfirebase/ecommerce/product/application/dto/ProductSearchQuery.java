package com.learnfirebase.ecommerce.product.application.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductSearchQuery {
    String search;
    String category;
    BigDecimal minPrice;
    BigDecimal maxPrice;
}
