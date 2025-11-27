package com.learnfirebase.ecommerce.inventory.infrastructure.persistence;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.learnfirebase.ecommerce.inventory.application.port.out.InventoryRepository;
import com.learnfirebase.ecommerce.inventory.domain.model.Inventory;
import com.learnfirebase.ecommerce.inventory.domain.model.InventoryId;
import com.learnfirebase.ecommerce.inventory.domain.model.InventoryItem;
import com.learnfirebase.ecommerce.inventory.domain.model.Warehouse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {
    private final InventoryJpaRepository inventoryJpaRepository;

    @Override
    public Inventory save(Inventory inventory) {
        InventoryEntity entity = toEntity(inventory);
        InventoryEntity saved = inventoryJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Inventory> findById(InventoryId id) {
        return inventoryJpaRepository.findById(id.getValue()).map(this::toDomain);
    }

    private InventoryEntity toEntity(Inventory inventory) {
        return InventoryEntity.builder()
            .id(inventory.getId().getValue())
            .warehouseId(inventory.getWarehouse() != null ? inventory.getWarehouse().getId() : null)
            .items(inventory.getItems().stream()
                .map(item -> InventoryItemEmbeddable.builder()
                    .productId(item.getProductId())
                    .available(item.getAvailable())
                    .reserved(item.getReserved())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }

    private Inventory toDomain(InventoryEntity entity) {
        return Inventory.builder()
            .id(new InventoryId(entity.getId()))
            .warehouse(entity.getWarehouseId() != null ? Warehouse.builder().id(entity.getWarehouseId()).name(entity.getWarehouseId()).build() : null)
            .items(entity.getItems().stream()
                .map(item -> InventoryItem.builder()
                    .productId(item.getProductId())
                    .available(item.getAvailable())
                    .reserved(item.getReserved())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
