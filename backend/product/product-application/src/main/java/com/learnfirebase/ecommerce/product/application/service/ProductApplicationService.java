package com.learnfirebase.ecommerce.product.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import com.learnfirebase.ecommerce.common.domain.valueobject.Money;
import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.product.application.command.UpsertProductCommand;
import com.learnfirebase.ecommerce.product.application.dto.ProductDto;
import com.learnfirebase.ecommerce.product.application.port.in.ManageProductUseCase;
import com.learnfirebase.ecommerce.product.application.port.in.QueryProductUseCase;
import com.learnfirebase.ecommerce.product.application.port.out.ProductEventPublisher;
import com.learnfirebase.ecommerce.product.application.port.out.ProductRepository;
import com.learnfirebase.ecommerce.product.application.port.out.ProductSearchIndexPort;
import com.learnfirebase.ecommerce.product.domain.exception.ProductDomainException;
import com.learnfirebase.ecommerce.product.domain.model.Category;
import com.learnfirebase.ecommerce.product.domain.model.Product;
import com.learnfirebase.ecommerce.product.domain.model.ProductId;
import com.learnfirebase.ecommerce.product.domain.model.ProductImage;
import com.learnfirebase.ecommerce.product.domain.model.ProductImageId;
import com.learnfirebase.ecommerce.product.domain.model.ProductVariant;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductApplicationService implements ManageProductUseCase, QueryProductUseCase {
    private final ProductRepository productRepository;
    private final ProductSearchIndexPort productSearchIndexPort;
    private final ProductEventPublisher eventPublisher;

    @Override
    public ProductDto execute(UpsertProductCommand command) {
        ProductId productId = new ProductId(command.getId() != null ? command.getId() : UUID.randomUUID().toString());
        Product product = productRepository.findById(productId).orElseGet(() -> Product.builder().id(productId).createdAt(Instant.now()).build());

        product = Product.builder()
            .id(productId)
            .name(command.getName())
            .description(command.getDescription())
            .price(Money.builder().amount(new BigDecimal(command.getPrice())).currency(command.getCurrency()).build())
            .category(command.getCategoryId() != null ? Category.builder().id(command.getCategoryId()).name(command.getCategoryId()).build() : null)
            .variants(command.getVariants() != null ? command.getVariants().stream()
                .map(v -> ProductVariant.builder()
                    .sku(v.getSku())
                    .name(v.getName())
                    .price(Money.builder().amount(new BigDecimal(v.getPrice())).currency(command.getCurrency()).build())
                    .build())
                .collect(Collectors.toList()) : java.util.Collections.emptyList())
            .images(command.getImages() != null ? command.getImages().stream()
                .map(img -> ProductImage.builder()
                    .id(img.getId() != null ? new ProductImageId(img.getId()) : new ProductImageId(UUID.randomUUID().toString()))
                    .url(img.getUrl())
                    .sortOrder(img.getSortOrder())
                    .primary(Boolean.TRUE.equals(img.getPrimaryImage()))
                    .build())
                .collect(Collectors.toList()) : java.util.Collections.emptyList())
            .createdAt(product.getCreatedAt() != null ? product.getCreatedAt() : Instant.now())
            .updatedAt(Instant.now())
            .build();

        Product saved = productRepository.save(product);
        productSearchIndexPort.index(saved);
        eventPublisher.publish(new com.learnfirebase.ecommerce.common.domain.DomainEvent() {});
        return toDto(saved);
    }

    @Override
    public PageResponse<ProductDto> listProducts(PageRequest pageRequest) {
        PageResponse<Product> page = productRepository.findAll(pageRequest);
        return PageResponse.<ProductDto>builder()
            .content(page.getContent().stream().map(this::toDto).toList())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .page(page.getPage())
            .size(page.getSize())
            .build();
    }

    @Override
    public ProductDto getProduct(String id) {
        ProductId productId = new ProductId(id);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductDomainException("Product not found: " + id));
        return toDto(product);
    }

    private ProductDto toDto(Product product) {
        if (product.getPrice() == null) {
            throw new ProductDomainException("Product price missing");
        }
        return ProductDto.builder()
            .id(product.getId().getValue())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice().getAmount().toPlainString())
            .currency(product.getPrice().getCurrency())
            .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .variants(product.getVariants() != null ? product.getVariants().stream()
                .map(v -> ProductDto.VariantDto.builder()
                    .sku(v.getSku())
                    .name(v.getName())
                    .price(v.getPrice().getAmount().toPlainString())
                    .build())
                .toList() : java.util.Collections.emptyList())
            .images(product.getImages() != null ? product.getImages().stream()
                .map(img -> ProductDto.ImageDto.builder()
                    .id(img.getId() != null ? img.getId().getValue() : null)
                    .url(img.getUrl())
                    .sortOrder(img.getSortOrder())
                    .primaryImage(img.isPrimary())
                    .build())
                .toList() : java.util.Collections.emptyList())
            .build();
    }
}
