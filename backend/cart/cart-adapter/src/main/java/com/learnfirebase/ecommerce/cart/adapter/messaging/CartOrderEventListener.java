package com.learnfirebase.ecommerce.cart.adapter.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.learnfirebase.ecommerce.cart.application.command.ClearCartCommand;
import com.learnfirebase.ecommerce.cart.application.port.in.ManageCartUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartOrderEventListener {

    private final ManageCartUseCase manageCartUseCase;
    private final ObjectMapper objectMapper;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @KafkaListener(topics = "order-events", groupId = "cart-service-group")
    public void handleOrderCreated(String message) {
        String eventId = null;
        String idempotencyKey = null;
        try {
            JsonNode root = objectMapper.readTree(message);
            eventId = root.has("eventId") ? root.get("eventId").asText() : null;
            String type = root.has("type") ? root.get("type").asText() : null;
            
            if (eventId == null || type == null) {
                return;
            }
            log.info("[EVENT-CONSUME] [Cart] Received eventId: {}, type: {}, payload: {}", eventId, type, message);
            
            if (!type.equals("OrderCreated")) {
                return;
            }

            // Idempotency check
            idempotencyKey = "processed_event:cart:" + eventId;
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "PROCESSING", java.time.Duration.ofHours(1));
            if (Boolean.FALSE.equals(isNew)) {
                log.info("Event {} already processing or processed by cart service, skipping", eventId);
                return;
            }

            try {
                JsonNode payload = root.has("payload") ? root.get("payload") : null;
                if (payload != null && payload.has("userId")) {
                    String userId = payload.get("userId").asText();
                    log.info("Clearing cart for user: {}", userId);
                    manageCartUseCase.clear(new ClearCartCommand(userId));
                }
                
                // Mark as done after success
                redisTemplate.opsForValue().set(idempotencyKey, "DONE", java.time.Duration.ofDays(1));
            } catch (Exception e) {
                // Clear the marker on failure so retry can process it
                redisTemplate.delete(idempotencyKey);
                throw e; // rethrow to log in outer catch
            }
        } catch (Exception e) {
            log.error("Failed to process order-events for cart clearing: {}", message, e);
            if (idempotencyKey != null) {
                throw new RuntimeException("Error processing event " + eventId, e);
            }
        }
    }
}
