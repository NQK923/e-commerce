package com.learnfirebase.ecommerce.order.infrastructure.outbox;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.common.domain.DomainEvent;
import com.learnfirebase.ecommerce.order.application.port.out.OrderOutboxPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderOutboxAdapter implements OrderOutboxPort {
    private final OutboxWorker outboxWorker;

    @Override
    public void saveEvent(DomainEvent event) {
        outboxWorker.saveRawEvent("order", event.getClass().getSimpleName(), event);
    }
}
