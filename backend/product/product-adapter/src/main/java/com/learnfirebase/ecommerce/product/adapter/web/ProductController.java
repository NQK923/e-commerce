package com.learnfirebase.ecommerce.product.adapter.web;

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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ManageProductUseCase manageProductUseCase;
    private final QueryProductUseCase queryProductUseCase;

    @GetMapping
    public ResponseEntity<PageResponse<ProductDto>> list(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "8") int size) {
        PageRequest pageRequest = PageRequest.builder().page(page).size(size).build();
        return ResponseEntity.ok(queryProductUseCase.listProducts(pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(queryProductUseCase.getProduct(id));
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@RequestBody UpsertProductCommand command) {
        return ResponseEntity.ok(manageProductUseCase.execute(command));
    }

    @PostMapping("/{id}")
    public ResponseEntity<ProductDto> update(@PathVariable String id, @RequestBody UpsertProductCommand command) {
        UpsertProductCommand merged = UpsertProductCommand.builder()
            .id(id)
            .name(command.getName())
            .description(command.getDescription())
            .price(command.getPrice())
            .currency(command.getCurrency())
            .categoryId(command.getCategoryId())
            .variants(command.getVariants())
            .images(command.getImages())
            .build();
        return ResponseEntity.ok(manageProductUseCase.execute(merged));
    }
}
