package com.learnfirebase.ecommerce.notification.adapter.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnfirebase.ecommerce.notification.application.command.RecordNotificationCommand;
import com.learnfirebase.ecommerce.notification.application.port.in.RecordNotificationUseCase;
import com.learnfirebase.ecommerce.notification.domain.model.NotificationChannel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationListener {

    private final RecordNotificationUseCase recordNotificationUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = {"OrderCreated", "OrderPaid", "OrderCancelled"}, groupId = "notification-service")
    public void handleOrderEvents(ConsumerRecord<String, String> record) {
        String topic = record.topic();
        String payload = record.value();
        try {
            EventEnvelope event = objectMapper.readValue(payload, EventEnvelope.class);
            if (event.userId == null) {
                log.debug("Skipping notification with missing userId for order {}", event.orderId);
                return;
            }
            String title;
            String body;
            switch (topic) {
                case "OrderCreated" -> {
                    title = "Order created";
                    body = "Order " + event.orderId + " has been created.";
                }
                case "OrderPaid" -> {
                    title = "Order paid";
                    body = "Order " + event.orderId + " was paid successfully.";
                }
                case "OrderCancelled" -> {
                    title = "Order cancelled";
                    body = "Order " + event.orderId + " has been cancelled.";
                }
                default -> {
                    return;
                }
            }
            recordNotificationUseCase.record(RecordNotificationCommand.builder()
                .userId(event.userId)
                .title(title)
                .body(body)
                .channel(NotificationChannel.PUSH)
                .build());
        } catch (Exception e) {
            log.error("Failed to handle order event for notifications", e);
        }
    }

    private static class EventEnvelope {
        public String orderId;
        public String userId;
    }
}
