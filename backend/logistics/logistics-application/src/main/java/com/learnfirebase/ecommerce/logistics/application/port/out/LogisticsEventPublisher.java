package com.learnfirebase.ecommerce.logistics.application.port.out;

import com.learnfirebase.ecommerce.common.domain.DomainEvent;

public interface LogisticsEventPublisher {
    void publish(DomainEvent event);
}
