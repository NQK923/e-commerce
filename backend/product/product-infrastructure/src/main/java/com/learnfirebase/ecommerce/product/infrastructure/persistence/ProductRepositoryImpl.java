package com.learnfirebase.ecommerce.product.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.common.domain.valueobject.Money;
import com.learnfirebase.ecommerce.product.application.port.out.ProductRepository;
import com.learnfirebase.ecommerce.product.domain.model.Category;
import com.learnfirebase.ecommerce.product.domain.model.Product;
import com.learnfirebase.ecommerce.product.domain.model.ProductId;
import com.learnfirebase.ecommerce.product.domain.model.ProductImage;
import com.learnfirebase.ecommerce.product.domain.model.ProductImageId;
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
    @Transactional(readOnly = true)
    public Optional<Product> findById(ProductId id) {
        return productJpaRepository.findById(id.getValue()).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<Product> findAll(PageRequest pageRequest) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getSize());
        org.springframework.data.domain.Page<ProductEntity> page = productJpaRepository.findAll(pageable);
        return PageResponse.<Product>builder()
            .content(page.getContent().stream().map(this::toDomain).toList())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .page(pageRequest.getPage())
            .size(pageRequest.getSize())
            .build();
    }

    private ProductEntity toEntity(Product product) {
        ProductEntity entity = ProductEntity.builder()
            .id(product.getId().getValue())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice().getAmount().toPlainString())
            .currency(product.getPrice().getCurrency())
            .quantity(product.getStock())
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
                .quantity(v.getQuantity())
                .product(entity)
                .build())
            .collect(Collectors.toList()) : java.util.Collections.emptyList());
        entity.setImages(product.getImages() != null ? product.getImages().stream()
            .map(img -> ProductImageEntity.builder()
                .id(img.getId() != null ? img.getId().getValue() : UUID.randomUUID().toString())
                .url(img.getUrl())
                .sortOrder(img.getSortOrder())
                .primaryImage(img.isPrimary())
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
            .stock(entity.getQuantity())
            .category(entity.getCategoryId() != null ? Category.builder().id(entity.getCategoryId()).name(entity.getCategoryId()).build() : null)
            .variants(entity.getVariants() != null ? entity.getVariants().stream()
                .map(v -> ProductVariant.builder()
                    .sku(v.getSku())
                    .name(v.getName())
                    .price(Money.builder().amount(new BigDecimal(v.getPrice())).currency(v.getCurrency()).build())
                    .quantity(v.getQuantity())
                    .build())
                .collect(Collectors.toList()) : java.util.Collections.emptyList())
            .images(entity.getImages() != null ? entity.getImages().stream()
                .map(img -> ProductImage.builder()
                    .id(new ProductImageId(img.getId()))
                    .url(img.getUrl())
                    .sortOrder(img.getSortOrder())
                    .primary(img.isPrimaryImage())
                    .build())
                .collect(Collectors.toList()) : java.util.Collections.emptyList())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
