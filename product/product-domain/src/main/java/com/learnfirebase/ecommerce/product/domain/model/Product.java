package com.learnfirebase.ecommerce.product.domain.model;

import java.time.Instant;
import java.util.List;

import com.learnfirebase.ecommerce.common.domain.AggregateRoot;
import com.learnfirebase.ecommerce.common.domain.valueobject.Money;

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
public class Product extends AggregateRoot<ProductId> {
    private ProductId id;
    private String name;
    private String description;
    private Money price;
    private Category category;
    private List<ProductVariant> variants;
    private Instant createdAt;
    private Instant updatedAt;
}
