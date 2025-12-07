package com.learnfirebase.ecommerce.product.application.command;

import java.util.List;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class UpsertProductCommand {
    String id;
    String name;
    String description;
    String price;
    String currency;
    String categoryId;
    Integer quantity;
    @Singular
    List<VariantCommand> variants;
    @Singular
    List<ImageCommand> images;

    @Value
    @Builder
    public static class VariantCommand {
        String sku;
        String name;
        String price;
        Integer quantity;
    }

    @Value
    @Builder
    public static class ImageCommand {
        String id;
        String url;
        Integer sortOrder;
        Boolean primaryImage;
    }
}
