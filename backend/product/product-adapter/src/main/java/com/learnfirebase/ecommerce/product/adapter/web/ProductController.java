package com.learnfirebase.ecommerce.product.adapter.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.product.application.command.UpsertProductCommand;
import com.learnfirebase.ecommerce.product.application.dto.ProductDto;
import com.learnfirebase.ecommerce.product.application.port.in.ManageProductUseCase;
import com.learnfirebase.ecommerce.product.application.port.in.QueryProductUseCase;
import com.learnfirebase.ecommerce.inventory.application.port.in.QueryInventoryUseCase;
import com.learnfirebase.ecommerce.inventory.application.dto.InventoryDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ManageProductUseCase manageProductUseCase;
    private final QueryProductUseCase queryProductUseCase;
    private final QueryInventoryUseCase queryInventoryUseCase;

    @GetMapping
    public ResponseEntity<PageResponse<ProductDto>> list(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "8") int size,
        @RequestParam(name = "includeOutOfStock", defaultValue = "false") boolean includeOutOfStock) {
        PageRequest pageRequest = PageRequest.builder().page(page).size(size).build();
        PageResponse<ProductDto> products = queryProductUseCase.listProducts(pageRequest);
        // Enrich with inventory (simplified for MVP, ideally should be batched)
        List<ProductDto> enriched = products.getContent().stream()
            .map(this::enrichWithInventory)
            .filter(p -> includeOutOfStock || p.getQuantity() == null || p.getQuantity() > 0)
            .toList();

        PageResponse<ProductDto> response = PageResponse.<ProductDto>builder()
            .content(enriched)
            .page(products.getPage())
            .size(products.getSize())
            .totalElements(products.getTotalElements())
            .totalPages(products.getTotalPages())
            .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(@PathVariable("id") String id) {
        ProductDto product = queryProductUseCase.getProduct(id);
        return ResponseEntity.ok(enrichWithInventory(product));
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@RequestBody UpsertProductCommand command) {
        return ResponseEntity.ok(manageProductUseCase.execute(command));
    }

    @PostMapping("/{id}")
    public ResponseEntity<ProductDto> update(@PathVariable("id") String id, @RequestBody UpsertProductCommand command) {
        UpsertProductCommand.UpsertProductCommandBuilder builder = UpsertProductCommand.builder()
            .id(id)
            .name(command.getName())
            .description(command.getDescription())
            .price(command.getPrice())
            .currency(command.getCurrency())
            .categoryId(command.getCategoryId())
            .quantity(command.getQuantity());

        if (command.getVariants() != null) {
            builder.variants(command.getVariants());
        }
        if (command.getImages() != null) {
            builder.images(command.getImages());
        }

        UpsertProductCommand merged = builder.build();
        return ResponseEntity.ok(manageProductUseCase.execute(merged));
    }
    
    private ProductDto enrichWithInventory(ProductDto product) {
        try {
            InventoryDto inventory = queryInventoryUseCase.getInventoryByProductId(product.getId());
            Integer totalStock = inventory.getItems().stream()
                .filter(item -> item.getProductId().equals(product.getId()))
                .map(InventoryDto.ItemDto::getAvailable)
                .findFirst().orElse(0);

            // Create a NEW ProductDto builder based on existing one (Dto is immutable)
            return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .currency(product.getCurrency())
                .categoryId(product.getCategoryId())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .quantity(totalStock)
                .images(product.getImages())
                .variants(product.getVariants().stream().map(v -> 
                    ProductDto.VariantDto.builder()
                        .sku(v.getSku())
                        .name(v.getName())
                        .price(v.getPrice())
                        .quantity(inventory.getItems().stream()
                            .filter(item -> item.getProductId().equals(v.getSku()))
                            .map(InventoryDto.ItemDto::getAvailable)
                            .findFirst().orElse(0))
                        .build()
                ).toList())
                .build();
        } catch (Exception e) {
            // If inventory service fails or no data, return product as is with 0 stock
            return product; 
        }
    }
}
