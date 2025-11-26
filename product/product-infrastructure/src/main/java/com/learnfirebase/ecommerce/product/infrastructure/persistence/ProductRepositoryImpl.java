package com.learnfirebase.ecommerce.product.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.learnfirebase.ecommerce.common.domain.valueobject.Money;
import com.learnfirebase.ecommerce.product.application.port.out.ProductRepository;
import com.learnfirebase.ecommerce.product.domain.model.Category;
import com.learnfirebase.ecommerce.product.domain.model.Product;
import com.learnfirebase.ecommerce.product.domain.model.ProductId;
import com.learnfirebase.ecommerce.product.domain.model.ProductVariant;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJpaRepository productJpaRepository;

    @Override
    public Product save(Product product) {
        ProductEntity entity = toEntity(product);
        ProductEntity saved = productJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return productJpaRepository.findById(id.getValue()).map(this::toDomain);
    }

    private ProductEntity toEntity(Product product) {
        ProductEntity entity = ProductEntity.builder()
            .id(product.getId().getValue())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice().getAmount().toPlainString())
            .currency(product.getPrice().getCurrency())
            .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();
        entity.setVariants(product.getVariants() != null ? product.getVariants().stream()
            .map(v -> ProductVariantEntity.builder()
                .sku(v.getSku() != null ? v.getSku() : UUID.randomUUID().toString())
                .name(v.getName())
                .price(v.getPrice().getAmount().toPlainString())
                .currency(v.getPrice().getCurrency())
                .product(entity)
                .build())
            .collect(Collectors.toList()) : java.util.Collections.emptyList());
        return entity;
    }

    private Product toDomain(ProductEntity entity) {
        return Product.builder()
            .id(new ProductId(entity.getId()))
            .name(entity.getName())
            .description(entity.getDescription())
            .price(Money.builder().amount(new BigDecimal(entity.getPrice())).currency(entity.getCurrency()).build())
            .category(entity.getCategoryId() != null ? Category.builder().id(entity.getCategoryId()).name(entity.getCategoryId()).build() : null)
            .variants(entity.getVariants() != null ? entity.getVariants().stream()
                .map(v -> ProductVariant.builder()
                    .sku(v.getSku())
                    .name(v.getName())
                    .price(Money.builder().amount(new BigDecimal(v.getPrice())).currency(v.getCurrency()).build())
                    .build())
                .collect(Collectors.toList()) : java.util.Collections.emptyList())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
