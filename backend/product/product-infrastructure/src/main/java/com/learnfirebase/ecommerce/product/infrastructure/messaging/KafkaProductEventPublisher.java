package com.learnfirebase.ecommerce.product.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.common.domain.DomainEvent;
import com.learnfirebase.ecommerce.product.application.port.out.ProductEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProductEventPublisher implements ProductEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(DomainEvent event) {
        try {
            kafkaTemplate.send("product-events", event);
        } catch (Exception ex) {
            log.error("Failed to publish product event", ex);
        }
    }
}
