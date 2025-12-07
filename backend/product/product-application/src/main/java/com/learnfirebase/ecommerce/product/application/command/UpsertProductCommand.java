package com.learnfirebase.ecommerce.product.application.command;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpsertProductCommand {
    private String id;
    private String name;
    private String description;
    private String price;
    private String currency;
    private String categoryId;
    private Integer quantity;
    @Singular
    private List<VariantCommand> variants;
    @Singular
    private List<ImageCommand> images;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantCommand {
        private String sku;
        private String name;
        private String price;
        private Integer quantity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageCommand {
        private String id;
        private String url;
        private Integer sortOrder;
        private Boolean primaryImage;
    }
}
