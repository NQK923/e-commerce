package com.learnfirebase.ecommerce.product.application.dto;

import java.util.List;
import java.util.Map;

import com.learnfirebase.ecommerce.product.domain.model.Product;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductSearchResult {
    List<Product> products;
    long totalElements;
    int totalPages;
    int page;
    int size;
    Map<String, Long> categoryFacets;
    List<String> suggestions;
}
