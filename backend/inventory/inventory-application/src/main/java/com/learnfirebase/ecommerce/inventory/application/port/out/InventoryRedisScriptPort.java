package com.learnfirebase.ecommerce.inventory.application.port.out;

import java.util.Map;

public interface InventoryRedisScriptPort {
    boolean executeAtomicReserve(String inventoryId, Map<String, Integer> reservations);
    int getAvailable(String inventoryId, String productId);
    int getReserved(String inventoryId, String productId);
}
