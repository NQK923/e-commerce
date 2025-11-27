package com.learnfirebase.ecommerce.inventory.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.learnfirebase.ecommerce.inventory.application.port.out.InventoryEventPublisher;
import com.learnfirebase.ecommerce.inventory.application.port.out.InventoryRedisScriptPort;
import com.learnfirebase.ecommerce.inventory.application.port.out.InventoryRepository;
import com.learnfirebase.ecommerce.inventory.application.service.InventoryApplicationService;
import com.learnfirebase.ecommerce.inventory.infrastructure.persistence.InventoryEntity;
import com.learnfirebase.ecommerce.inventory.infrastructure.persistence.InventoryJpaRepository;

@Configuration
@EnableJpaRepositories(basePackageClasses = InventoryJpaRepository.class)
@EntityScan(basePackageClasses = InventoryEntity.class)
public class InventoryModuleConfig {
    @Bean
    public InventoryApplicationService inventoryApplicationService(InventoryRepository inventoryRepository, InventoryRedisScriptPort inventoryRedisScriptPort, InventoryEventPublisher inventoryEventPublisher) {
        return new InventoryApplicationService(inventoryRepository, inventoryRedisScriptPort, inventoryEventPublisher);
    }
}
