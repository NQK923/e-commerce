package com.learnfirebase.ecommerce.order.infrastructure.outbox;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.common.domain.DomainEvent;
import com.learnfirebase.ecommerce.order.application.port.out.OrderOutboxPort;
import com.learnfirebase.ecommerce.order.domain.event.OrderCreated;
import com.learnfirebase.ecommerce.order.domain.event.OrderPaid;
import com.learnfirebase.ecommerce.order.domain.event.OrderCancelled;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderOutboxAdapter implements OrderOutboxPort {
    private final OutboxWorker outboxWorker;

    @Override
    public void saveEvent(DomainEvent event) {
        String aggregateId = "order";
        if (event instanceof OrderCreated oe) {
            aggregateId = oe.getOrderId();
        } else if (event instanceof OrderPaid oe) {
            aggregateId = oe.getOrderId();
        } else if (event instanceof OrderCancelled oe) {
            aggregateId = oe.getOrderId();
        }
        outboxWorker.saveRawEvent(aggregateId, event.getClass().getSimpleName(), event);
    }
}
