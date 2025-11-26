package com.learnfirebase.ecommerce.product.application.port.out;

import com.learnfirebase.ecommerce.common.domain.DomainEvent;

public interface ProductEventPublisher {
    void publish(DomainEvent event);
}
