package com.learnfirebase.ecommerce.product.adapter.web;

import java.math.BigDecimal;
import java.security.Principal;
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
import com.learnfirebase.ecommerce.product.application.dto.ProductSearchQuery;
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
        @RequestParam(name = "search", required = false) String search,
        @RequestParam(name = "category", required = false) String category,
        @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
        @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
        @RequestParam(name = "sellerId", required = false) String sellerId,
        @RequestParam(name = "sort", required = false) String sort,
        @RequestParam(name = "includeOutOfStock", defaultValue = "false") boolean includeOutOfStock) {
        
        PageRequest pageRequest = PageRequest.builder().page(page).size(size).sort(sort).build();
        ProductSearchQuery query = ProductSearchQuery.builder()
            .search(search)
            .category(category)
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .sellerId(sellerId)
            .sort(sort)
            .size(size)
            .build();
            
        PageResponse<ProductDto> products = queryProductUseCase.searchProducts(query, pageRequest);
        
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

    @GetMapping("/suggest")
    public ResponseEntity<List<ProductDto>> suggest(
        @RequestParam("prefix") String prefix,
        @RequestParam(name = "limit", defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(queryProductUseCase.suggestProducts(prefix, limit));
    }

    @GetMapping("/{id}/similar")
    public ResponseEntity<List<ProductDto>> similar(
        @PathVariable("id") String id,
        @RequestParam(name = "limit", defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(queryProductUseCase.similarProducts(id, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(@PathVariable("id") String id) {
        ProductDto product = queryProductUseCase.getProduct(id);
        return ResponseEntity.ok(enrichWithInventory(product));
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@RequestBody UpsertProductCommand command, Principal principal) {
        if (principal != null) {
            command.setSellerId(principal.getName());
        }
        return ResponseEntity.ok(manageProductUseCase.execute(command));
    }

    @PostMapping("/{id}")
    public ResponseEntity<ProductDto> update(@PathVariable("id") String id, @RequestBody UpsertProductCommand command, Principal principal) {
        UpsertProductCommand.UpsertProductCommandBuilder builder = UpsertProductCommand.builder()
            .id(id)
            .name(command.getName())
            .description(command.getDescription())
            .price(command.getPrice())
            .currency(command.getCurrency())
            .categoryId(command.getCategoryId())
            .quantity(command.getQuantity());

        if (principal != null) {
            builder.sellerId(principal.getName());
        }

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
                .findFirst()
                .orElse(product.getQuantity() != null ? product.getQuantity() : 0);

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
                .sellerId(product.getSellerId())
                .images(product.getImages())
                .variants(product.getVariants().stream().map(v -> 
                    ProductDto.VariantDto.builder()
                        .sku(v.getSku())
                        .name(v.getName())
                        .price(v.getPrice())
                        .quantity(inventory.getItems().stream()
                            .filter(item -> item.getProductId().equals(v.getSku()))
                            .map(InventoryDto.ItemDto::getAvailable)
                            .findFirst()
                            .orElse(v.getQuantity() != null ? v.getQuantity() : 0))
                        .build()
                ).toList())
                .build();
        } catch (Exception e) {
            // If inventory service fails or no data, return product as is with 0 stock
            return product; 
        }
    }
}
