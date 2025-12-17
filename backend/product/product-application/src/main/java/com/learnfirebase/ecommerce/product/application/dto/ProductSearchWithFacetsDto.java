package com.learnfirebase.ecommerce.product.application.dto;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductSearchWithFacetsDto {
    List<ProductDto> items;
    long totalElements;
    int totalPages;
    int page;
    int size;
    Map<String, Long> categoryFacets;
    List<String> suggestions;
}
