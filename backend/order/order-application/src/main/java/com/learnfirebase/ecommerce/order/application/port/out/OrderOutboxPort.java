package com.learnfirebase.ecommerce.order.application.port.out;

import com.learnfirebase.ecommerce.common.domain.DomainEvent;

public interface OrderOutboxPort {
    void saveEvent(DomainEvent event);
}
