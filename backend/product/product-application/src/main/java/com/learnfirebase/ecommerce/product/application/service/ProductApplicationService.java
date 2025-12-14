package com.learnfirebase.ecommerce.product.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
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

import com.learnfirebase.ecommerce.product.application.dto.ProductSearchQuery;
import com.learnfirebase.ecommerce.product.application.dto.SoldItemDto;
import com.learnfirebase.ecommerce.product.domain.event.ProductCreatedEvent;

@RequiredArgsConstructor
public class ProductApplicationService implements ManageProductUseCase, QueryProductUseCase {
    private final ProductRepository productRepository;
    private final ProductSearchIndexPort productSearchIndexPort;
    private final ProductEventPublisher eventPublisher;

    public void handleOrderPaid(List<SoldItemDto> items) {
        if (items == null || items.isEmpty()) return;
        for (SoldItemDto item : items) {
            if (item.getProductId() != null && item.getQuantity() > 0) {
                try {
                    productRepository.incrementSoldCount(new ProductId(item.getProductId()), item.getQuantity());
                } catch (Exception e) {
                    System.err.println("Failed to increment sold count for product " + item.getProductId() + ": " + e.getMessage());
                }
            }
        }
    }

    @Override
    public PageResponse<ProductDto> searchProducts(ProductSearchQuery query, PageRequest pageRequest) {
        PageResponse<Product> page = productRepository.search(query, pageRequest);
        return PageResponse.<ProductDto>builder()
            .content(page.getContent().stream().map(this::toDto).toList())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .page(page.getPage())
            .size(page.getSize())
            .build();
    }

    @Override
    public ProductDto execute(UpsertProductCommand command) {
        System.out.println("DEBUG: Starting execute for product: " + command.getName());
        try {
            ProductId productId = new ProductId(command.getId() != null ? command.getId() : UUID.randomUUID().toString());
            Product existingProduct = productRepository.findById(productId).orElse(null);

            String priceStr = command.getPrice();
            if (priceStr == null || priceStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Product price is required");
            }

            List<ProductVariant> variantsToUse = command.getVariants() != null ? command.getVariants().stream()
                .map(v -> {
                    String vPrice = v.getPrice();
                    if (vPrice == null || vPrice.trim().isEmpty()) {
                         throw new IllegalArgumentException("Variant price is required for SKU: " + v.getSku());
                    }
                    return ProductVariant.builder()
                        .sku(v.getSku())
                        .name(v.getName())
                        .price(Money.builder().amount(new BigDecimal(vPrice)).currency(command.getCurrency()).build())
                        .quantity(v.getQuantity())
                        .build();
                })
                .collect(Collectors.toList()) : (existingProduct != null ? existingProduct.getVariants() : Collections.emptyList());

            List<ProductImage> imagesToUse = command.getImages() != null ? command.getImages().stream()
                .map(img -> ProductImage.builder()
                    .id(img.getId() != null ? new ProductImageId(img.getId()) : new ProductImageId(UUID.randomUUID().toString()))
                    .url(img.getUrl())
                    .sortOrder(img.getSortOrder())
                    .primary(Boolean.TRUE.equals(img.getPrimaryImage()))
                    .build())
                .collect(Collectors.toList()) : (existingProduct != null ? existingProduct.getImages() : Collections.emptyList());

            Product product = existingProduct != null ? existingProduct : Product.builder().id(productId).createdAt(Instant.now()).build();

            product = Product.builder()
                .id(productId)
                .name(command.getName())
                .description(command.getDescription())
                .price(Money.builder().amount(new BigDecimal(priceStr)).currency(command.getCurrency()).build())
                .stock(command.getQuantity())
                .sellerId(command.getSellerId())
                .category(command.getCategoryId() != null ? Category.builder().id(command.getCategoryId()).name(command.getCategoryId()).build() : null)
                .variants(variantsToUse)
                .images(imagesToUse)
                .createdAt(product.getCreatedAt() != null ? product.getCreatedAt() : Instant.now())
                .updatedAt(Instant.now())
                .build();

            System.out.println("DEBUG: Saving product to repository");
            Product saved = productRepository.save(product);
            System.out.println("DEBUG: Product saved. Indexing...");
            
            try {
                productSearchIndexPort.index(saved);
            } catch (Exception e) {
                System.err.println("DEBUG: Search indexing failed: " + e.getMessage());
                // Non-critical?
            }

            if (existingProduct == null) {
                System.out.println("DEBUG: Publishing event (New Product)");
                eventPublisher.publish(ProductCreatedEvent.builder()
                    .productId(saved.getId().getValue())
                    .initialStock(command.getQuantity())
                    .variants(command.getVariants() != null ? command.getVariants().stream()
                        .map(v -> ProductCreatedEvent.VariantInitialStock.builder()
                            .sku(v.getSku())
                            .quantity(v.getQuantity() != null ? v.getQuantity() : 0)
                            .build())
                        .collect(Collectors.toList()) : java.util.Collections.emptyList())
                    .build());
            } else {
                System.out.println("DEBUG: Skipping event publication for Product Update to prevent inventory duplication.");
            }
            
            System.out.println("DEBUG: Event published (if new). Returning DTO");
            return toDto(saved);
        } catch (Exception e) {
            System.err.println("DEBUG: Error in execute: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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
            .quantity(product.getStock())
            .soldCount(product.getSoldCount() != null ? product.getSoldCount() : 0)
            .sellerId(product.getSellerId())
            .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .variants(product.getVariants() != null ? product.getVariants().stream()
                .map(v -> ProductDto.VariantDto.builder()
                    .sku(v.getSku())
                    .name(v.getName())
                    .price(v.getPrice().getAmount().toPlainString())
                    .quantity(v.getQuantity())
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
