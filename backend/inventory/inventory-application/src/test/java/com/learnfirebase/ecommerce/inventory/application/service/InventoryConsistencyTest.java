package com.learnfirebase.ecommerce.inventory.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.learnfirebase.ecommerce.inventory.application.command.AdjustInventoryCommand;
import com.learnfirebase.ecommerce.inventory.application.dto.InventoryDto;
import com.learnfirebase.ecommerce.inventory.application.port.out.InventoryEventPublisher;
import com.learnfirebase.ecommerce.inventory.application.port.out.InventoryRedisScriptPort;
import com.learnfirebase.ecommerce.inventory.application.port.out.InventoryRepository;
import com.learnfirebase.ecommerce.inventory.domain.exception.InventoryDomainException;
import com.learnfirebase.ecommerce.inventory.domain.model.Inventory;
import com.learnfirebase.ecommerce.inventory.domain.model.InventoryId;
import com.learnfirebase.ecommerce.inventory.domain.model.InventoryItem;
import com.learnfirebase.ecommerce.inventory.domain.model.Warehouse;

class InventoryConsistencyTest {

    private InventoryRepository inventoryRepository;
    private InventoryRedisScriptPort redisScriptPort;
    private InventoryEventPublisher eventPublisher;
    private InventoryApplicationService service;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(InventoryRepository.class);
        redisScriptPort = mock(InventoryRedisScriptPort.class);
        eventPublisher = mock(InventoryEventPublisher.class);
        service = new InventoryApplicationService(inventoryRepository, redisScriptPort, eventPublisher);
    }

    @Test
    void executeAdjustmentUpdatesDbAndRedis() {
        InventoryId invId = new InventoryId("DEFAULT_INVENTORY");
        Inventory mockInventory = Inventory.builder()
                .id(invId)
                .warehouse(Warehouse.builder().id("default").name("Default").build())
                .items(new ArrayList<>())
                .build();

        when(inventoryRepository.findById(eq(invId))).thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(redisScriptPort.executeAtomicReserve(eq("DEFAULT_INVENTORY"), any())).thenReturn(true);
        when(redisScriptPort.getAvailable(eq("DEFAULT_INVENTORY"), eq("prod-1"))).thenReturn(50);
        when(redisScriptPort.getReserved(eq("DEFAULT_INVENTORY"), eq("prod-1"))).thenReturn(5);

        AdjustInventoryCommand command = AdjustInventoryCommand.builder()
                .inventoryId("DEFAULT_INVENTORY")
                .productId("prod-1")
                .delta(50)
                .build();

        InventoryDto result = service.execute(command);

        assertNotNull(result);
        assertEquals("DEFAULT_INVENTORY", result.getId());
        assertEquals(1, result.getItems().size());
        assertEquals("prod-1", result.getItems().get(0).getProductId());
        assertEquals(50, result.getItems().get(0).getAvailable());
        assertEquals(5, result.getItems().get(0).getReserved());

        verify(inventoryRepository).save(any(Inventory.class));
        verify(redisScriptPort).executeAtomicReserve(eq("DEFAULT_INVENTORY"), any());
        verify(eventPublisher).publish(any());
    }

    @Test
    void executeAdjustmentThrowsOnInsufficientStock() {
        InventoryId invId = new InventoryId("DEFAULT_INVENTORY");
        ArrayList<InventoryItem> items = new ArrayList<>();
        items.add(InventoryItem.builder().productId("prod-1").available(10).reserved(0).build());
        
        Inventory mockInventory = Inventory.builder()
                .id(invId)
                .warehouse(Warehouse.builder().id("default").name("Default").build())
                .items(items)
                .build();

        when(inventoryRepository.findById(eq(invId))).thenReturn(Optional.of(mockInventory));

        AdjustInventoryCommand command = AdjustInventoryCommand.builder()
                .inventoryId("DEFAULT_INVENTORY")
                .productId("prod-1")
                .delta(-15) // Adjusting below 0
                .build();

        assertThrows(InventoryDomainException.class, () -> service.execute(command));
    }
}
