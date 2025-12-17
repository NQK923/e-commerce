package com.learnfirebase.ecommerce.inventory.application.service;

import java.util.UUID;
import java.util.stream.Collectors;

import com.learnfirebase.ecommerce.inventory.application.command.AdjustInventoryCommand;
import com.learnfirebase.ecommerce.inventory.application.dto.InventoryDto;
import com.learnfirebase.ecommerce.inventory.application.port.in.ManageInventoryUseCase;
import com.learnfirebase.ecommerce.inventory.application.port.out.InventoryEventPublisher;
import com.learnfirebase.ecommerce.inventory.application.port.out.InventoryRedisScriptPort;
import com.learnfirebase.ecommerce.inventory.application.port.out.InventoryRepository;
import com.learnfirebase.ecommerce.inventory.domain.exception.InventoryDomainException;
import com.learnfirebase.ecommerce.inventory.domain.model.Inventory;
import com.learnfirebase.ecommerce.inventory.domain.model.InventoryId;
import com.learnfirebase.ecommerce.inventory.domain.model.Warehouse;

import lombok.RequiredArgsConstructor;

import com.learnfirebase.ecommerce.inventory.application.port.in.QueryInventoryUseCase;

@RequiredArgsConstructor
public class InventoryApplicationService implements ManageInventoryUseCase, QueryInventoryUseCase {
    private final InventoryRepository inventoryRepository;
    private final InventoryRedisScriptPort redisScriptPort;
    private final InventoryEventPublisher eventPublisher;
    private static final String DEFAULT_INVENTORY_ID = "DEFAULT_INVENTORY";

    @Override
    public InventoryDto execute(AdjustInventoryCommand command) {
        String inventoryId = command.getInventoryId() != null ? command.getInventoryId() : DEFAULT_INVENTORY_ID;
        Inventory inventory = inventoryRepository.findById(new InventoryId(inventoryId))
            .orElse(Inventory.builder()
                .id(new InventoryId(inventoryId != null ? inventoryId : UUID.randomUUID().toString()))
                .warehouse(Warehouse.builder().id("default").name("Default").build())
                .build());
        inventory.adjust(command.getProductId(), command.getDelta());
        Inventory saved = inventoryRepository.save(inventory);
        redisScriptPort.executeAtomicReserve(saved.getId().getValue(), java.util.Map.of(command.getProductId(), command.getDelta()));
        eventPublisher.publish(new com.learnfirebase.ecommerce.common.domain.DomainEvent() {});
        return toDto(saved);
    }

    @Override
    public InventoryDto getInventoryByProductId(String productId) {
        // Since we only have a default inventory for now, we scan for it.
        // Ideally we should have a lookup table or index by productId
        // For now, we'll fetch the default inventory
        Inventory inventory = inventoryRepository.findById(new InventoryId(DEFAULT_INVENTORY_ID))
             .orElseThrow(() -> new InventoryDomainException("Default inventory not found"));
        
        return toDto(inventory);
    }

    private InventoryDto toDto(Inventory inventory) {
        if (inventory.getId() == null) {
            throw new InventoryDomainException("Inventory missing id");
        }
        return InventoryDto.builder()
            .id(inventory.getId().getValue())
            .warehouseId(inventory.getWarehouse() != null ? inventory.getWarehouse().getId() : null)
            .items(inventory.getItems().stream()
                .map(item -> InventoryDto.ItemDto.builder()
                    .productId(item.getProductId())
                    .available(resolveAvailable(inventory.getId(), item))
                    .reserved(resolveReserved(inventory.getId(), item))
                    .build())
                .collect(Collectors.toList()))
            .build();
    }

    private int resolveAvailable(InventoryId inventoryId, com.learnfirebase.ecommerce.inventory.domain.model.InventoryItem item) {
        int available = redisScriptPort.getAvailable(inventoryId.getValue(), item.getProductId());
        if (available == 0) {
            available = item.getAvailable();
        }
        return available;
    }

    private int resolveReserved(InventoryId inventoryId, com.learnfirebase.ecommerce.inventory.domain.model.InventoryItem item) {
        int reserved = redisScriptPort.getReserved(inventoryId.getValue(), item.getProductId());
        if (reserved == 0) {
            reserved = item.getReserved();
        }
        return reserved;
    }
}
