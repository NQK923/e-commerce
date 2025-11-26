package com.learnfirebase.ecommerce.inventory.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.learnfirebase.ecommerce.inventory.application.port.out.InventoryEventPublisher;
import com.learnfirebase.ecommerce.inventory.application.port.out.InventoryRedisScriptPort;
import com.learnfirebase.ecommerce.inventory.application.port.out.InventoryRepository;
import com.learnfirebase.ecommerce.inventory.application.service.InventoryApplicationService;

@Configuration
public class InventoryModuleConfig {
    @Bean
    public InventoryApplicationService inventoryApplicationService(InventoryRepository inventoryRepository, InventoryRedisScriptPort inventoryRedisScriptPort, InventoryEventPublisher inventoryEventPublisher) {
        return new InventoryApplicationService(inventoryRepository, inventoryRedisScriptPort, inventoryEventPublisher);
    }
}
