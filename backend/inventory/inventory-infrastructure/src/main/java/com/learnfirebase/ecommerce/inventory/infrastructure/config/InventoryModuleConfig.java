package com.learnfirebase.ecommerce.inventory.infrastructure.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.learnfirebase.ecommerce.inventory.domain.model.InventoryId;
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
    private static final String DEFAULT_INVENTORY_ID = "DEFAULT_INVENTORY";

    @Bean
    public InventoryApplicationService inventoryApplicationService(InventoryRepository inventoryRepository, InventoryRedisScriptPort inventoryRedisScriptPort, InventoryEventPublisher inventoryEventPublisher) {
        return new InventoryApplicationService(inventoryRepository, inventoryRedisScriptPort, inventoryEventPublisher);
    }

    @Bean
    public ApplicationRunner inventoryRedisWarmup(InventoryRepository inventoryRepository, StringRedisTemplate redisTemplate) {
        return args -> inventoryRepository.findById(new InventoryId(DEFAULT_INVENTORY_ID)).ifPresent(inventory ->
            inventory.getItems().forEach(item -> {
                String availableKey = "inventory:%s:%s:available".formatted(DEFAULT_INVENTORY_ID, item.getProductId());
                String reservedKey = "inventory:%s:%s:reserved".formatted(DEFAULT_INVENTORY_ID, item.getProductId());
                redisTemplate.opsForValue().setIfAbsent(availableKey, String.valueOf(item.getAvailable()));
                redisTemplate.opsForValue().setIfAbsent(reservedKey, String.valueOf(item.getReserved()));
            })
        );
    }
}
