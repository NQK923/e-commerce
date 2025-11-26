package com.learnfirebase.ecommerce.inventory.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.common.domain.DomainEvent;
import com.learnfirebase.ecommerce.inventory.application.port.out.InventoryEventPublisher;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KafkaInventoryEventPublisher implements InventoryEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(DomainEvent event) {
        kafkaTemplate.send("inventory-events", event);
    }
}
