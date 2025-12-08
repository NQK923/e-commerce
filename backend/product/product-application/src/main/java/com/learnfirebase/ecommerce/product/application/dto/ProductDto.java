package com.learnfirebase.ecommerce.product.application.dto;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class ProductDto {
    String id;
    String name;
    String description;
    String price;
    String currency;
    String categoryId;
    Integer quantity;
    Integer soldCount;
    Instant createdAt;
    Instant updatedAt;
    @Singular
    List<VariantDto> variants;
    @Singular
    List<ImageDto> images;

    @Value
    @Builder
    public static class VariantDto {
        String sku;
        String name;
        String price;
        Integer quantity;
    }

    @Value
    @Builder
    public static class ImageDto {
        String id;
        String url;
        Integer sortOrder;
        Boolean primaryImage;
    }
}
