package com.learnfirebase.ecommerce.logistics.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.common.domain.DomainEvent;
import com.learnfirebase.ecommerce.logistics.application.port.out.LogisticsEventPublisher;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KafkaLogisticsEventPublisher implements LogisticsEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(DomainEvent event) {
        kafkaTemplate.send("logistics-events", event);
    }
}
