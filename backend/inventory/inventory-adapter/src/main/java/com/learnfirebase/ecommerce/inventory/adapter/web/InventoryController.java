package com.learnfirebase.ecommerce.inventory.adapter.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.inventory.application.command.AdjustInventoryCommand;
import com.learnfirebase.ecommerce.inventory.application.dto.InventoryDto;
import com.learnfirebase.ecommerce.inventory.application.port.in.ManageInventoryUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final ManageInventoryUseCase manageInventoryUseCase;

    @PostMapping
    public ResponseEntity<InventoryDto> adjust(@RequestBody AdjustInventoryCommand command) {
        return ResponseEntity.ok(manageInventoryUseCase.execute(command));
    }
}
