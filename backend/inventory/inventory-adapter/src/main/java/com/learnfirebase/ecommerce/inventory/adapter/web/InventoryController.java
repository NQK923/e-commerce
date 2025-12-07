package com.learnfirebase.ecommerce.inventory.adapter.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.inventory.application.dto.InventoryDto;
import com.learnfirebase.ecommerce.inventory.application.port.in.QueryInventoryUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final QueryInventoryUseCase queryInventoryUseCase;

    @GetMapping("/product/{productId}")
    public ResponseEntity<InventoryDto> getByProductId(@PathVariable String productId) {
        return ResponseEntity.ok(queryInventoryUseCase.getInventoryByProductId(productId));
    }
}