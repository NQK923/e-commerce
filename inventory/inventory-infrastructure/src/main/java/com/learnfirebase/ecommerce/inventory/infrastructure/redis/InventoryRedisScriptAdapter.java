package com.learnfirebase.ecommerce.inventory.infrastructure.redis;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.inventory.application.port.out.InventoryRedisScriptPort;

@Component
public class InventoryRedisScriptAdapter implements InventoryRedisScriptPort {
    @Override
    public boolean executeAtomicReserve(String inventoryId, Map<String, Integer> reservations) {
        return true;
    }
}
