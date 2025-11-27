package com.learnfirebase.ecommerce.product.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ProductImage {
    private ProductImageId id;
    private String url;
    private Integer sortOrder;
    @Builder.Default
    private boolean primary = false;
}
