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

    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @KafkaListener(topics = "order-events", groupId = "notification-service")
    public void handleOrderEvents(ConsumerRecord<String, String> record) {
        String message = record.value();
        String eventId = null;
        String idempotencyKey = null;
        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(message);
            eventId = root.has("eventId") ? root.get("eventId").asText() : root.has("id") ? root.get("id").asText() : null;
            String type = root.has("type") ? root.get("type").asText() : null;
            
            if (eventId == null || type == null) {
                return;
            }
            log.info("[EVENT-CONSUME] [Notification] Received eventId: {}, type: {}, payload: {}", eventId, type, message);

            // Idempotency check
            idempotencyKey = "processed_event:notification:" + eventId;
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "PROCESSING", java.time.Duration.ofHours(1));
            if (Boolean.FALSE.equals(isNew)) {
                log.info("Event {} already processing or processed by notification service, skipping", eventId);
                return;
            }

            try {
                com.fasterxml.jackson.databind.JsonNode payloadNode = root.get("payload");
                if (payloadNode == null) return;
                
                String payloadText = payloadNode.isTextual() ? payloadNode.asText() : payloadNode.toString();
                EventEnvelope event = objectMapper.readValue(payloadText, EventEnvelope.class);
                
                if (event.userId == null) {
                    log.debug("Skipping notification with missing userId for order {}", event.orderId);
                    redisTemplate.opsForValue().set(idempotencyKey, "DONE", java.time.Duration.ofDays(1));
                    return;
                }
                String title;
                String body;
                switch (type) {
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
                        redisTemplate.opsForValue().set(idempotencyKey, "DONE", java.time.Duration.ofDays(1));
                        return;
                    }
                }
                recordNotificationUseCase.record(RecordNotificationCommand.builder()
                    .userId(event.userId)
                    .title(title)
                    .body(body)
                    .channel(NotificationChannel.PUSH)
                    .build());
                    
                redisTemplate.opsForValue().set(idempotencyKey, "DONE", java.time.Duration.ofDays(1));
            } catch (Exception e) {
                redisTemplate.delete(idempotencyKey);
                throw e;
            }
        } catch (Exception e) {
            log.error("Failed to handle order event for notifications", e);
            if (idempotencyKey != null) {
                throw new RuntimeException("Error processing event " + eventId, e);
            }
        }
    }

    private static class EventEnvelope {
        public String orderId;
        public String userId;
    }
}
