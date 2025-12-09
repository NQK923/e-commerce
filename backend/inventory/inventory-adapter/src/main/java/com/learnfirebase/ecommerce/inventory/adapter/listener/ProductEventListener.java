package com.learnfirebase.ecommerce.inventory.adapter.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.product.domain.event.ProductCreatedEvent;
import com.learnfirebase.ecommerce.inventory.application.command.AdjustInventoryCommand;
import com.learnfirebase.ecommerce.inventory.application.port.in.ManageInventoryUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventListener {
    private final ManageInventoryUseCase manageInventoryUseCase;
    private static final String DEFAULT_INVENTORY_ID = "DEFAULT_INVENTORY";

    @EventListener
    public void handle(ProductCreatedEvent event) {
        log.info("Received ProductCreatedEvent for product: {}, initialStock: {}, variants: {}", 
            event.getProductId(), event.getInitialStock(), event.getVariants() != null ? event.getVariants().size() : "null");
        
        if (event.getInitialStock() != null && event.getInitialStock() > 0) {
            log.info("Adjusting inventory for main product: {}, delta: {}", event.getProductId(), event.getInitialStock());
            manageInventoryUseCase.execute(AdjustInventoryCommand.builder()
                .inventoryId(DEFAULT_INVENTORY_ID)
                .productId(event.getProductId())
                .delta(event.getInitialStock())
                .build());
        } else {
            log.info("Skipping main product inventory adjustment. InitialStock is null or <= 0");
        }

        if (event.getVariants() != null) {
            for (ProductCreatedEvent.VariantInitialStock variant : event.getVariants()) {
                if (variant.getQuantity() > 0) {
                    log.info("Adjusting inventory for variant: {}, delta: {}", variant.getSku(), variant.getQuantity());
                    manageInventoryUseCase.execute(AdjustInventoryCommand.builder()
                        .inventoryId(DEFAULT_INVENTORY_ID)
                        .productId(variant.getSku())
                        .delta(variant.getQuantity())
                        .build());
                }
            }
        }
    }
}
