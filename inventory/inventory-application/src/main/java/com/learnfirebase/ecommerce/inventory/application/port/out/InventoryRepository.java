package com.learnfirebase.ecommerce.inventory.application.port.out;

import java.util.Optional;

import com.learnfirebase.ecommerce.inventory.domain.model.Inventory;
import com.learnfirebase.ecommerce.inventory.domain.model.InventoryId;

public interface InventoryRepository {
    Inventory save(Inventory inventory);

    Optional<Inventory> findById(InventoryId id);
}
