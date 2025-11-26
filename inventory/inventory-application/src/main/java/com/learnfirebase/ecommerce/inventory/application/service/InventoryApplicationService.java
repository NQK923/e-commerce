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
import com.learnfirebase.ecommerce.inventory.domain.model.InventoryItem;
import com.learnfirebase.ecommerce.inventory.domain.model.Warehouse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InventoryApplicationService implements ManageInventoryUseCase {
    private final InventoryRepository inventoryRepository;
    private final InventoryRedisScriptPort redisScriptPort;
    private final InventoryEventPublisher eventPublisher;

    @Override
    public InventoryDto execute(AdjustInventoryCommand command) {
        Inventory inventory = inventoryRepository.findById(new InventoryId(command.getInventoryId()))
            .orElse(Inventory.builder()
                .id(new InventoryId(command.getInventoryId() != null ? command.getInventoryId() : UUID.randomUUID().toString()))
                .warehouse(Warehouse.builder().id("default").name("Default").build())
                .build());
        inventory.adjust(command.getProductId(), command.getDelta());
        Inventory saved = inventoryRepository.save(inventory);
        redisScriptPort.executeAtomicReserve(saved.getId().getValue(), java.util.Map.of(command.getProductId(), command.getDelta()));
        eventPublisher.publish(new com.learnfirebase.ecommerce.common.domain.DomainEvent() {});
        return toDto(saved);
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
                    .available(item.getAvailable())
                    .reserved(item.getReserved())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
