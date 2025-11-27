package com.learnfirebase.ecommerce.order.adapter.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderKafkaConsumer {
    @KafkaListener(topics = "order-events", groupId = "order-service")
    public void handle(String payload) {
        log.info("Received external order event: {}", payload);
    }
}
