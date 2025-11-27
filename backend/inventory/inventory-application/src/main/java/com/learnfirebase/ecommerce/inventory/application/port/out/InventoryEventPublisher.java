package com.learnfirebase.ecommerce.inventory.application.port.out;

import com.learnfirebase.ecommerce.common.domain.DomainEvent;

public interface InventoryEventPublisher {
    void publish(DomainEvent event);
}
