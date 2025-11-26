package com.learnfirebase.ecommerce.order.application.port.out;

import com.learnfirebase.ecommerce.common.domain.DomainEvent;

public interface OrderEventPublisher {
    void publish(DomainEvent event);
}
