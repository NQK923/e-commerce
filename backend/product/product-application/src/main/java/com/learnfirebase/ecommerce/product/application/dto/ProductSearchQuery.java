package com.learnfirebase.ecommerce.product.application.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductSearchQuery {
    String search;
    String category;
    BigDecimal minPrice;
    BigDecimal maxPrice;
    String sellerId;
    String sort;
    Integer size;
    List<String> tags; // Filter by product tags
    Integer minRating; // Minimum average rating (1-5)
    Boolean inStock; // Filter only products in stock
}
