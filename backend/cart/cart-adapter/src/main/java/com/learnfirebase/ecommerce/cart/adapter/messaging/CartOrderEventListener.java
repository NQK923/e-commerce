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

    @KafkaListener(topics = "order-created", groupId = "cart-service-group")
    public void handleOrderCreated(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            // Assuming the structure matches OrderCreated event fields
            if (root.has("userId")) {
                String userId = root.get("userId").asText();
                log.info("Clearing cart for user: {}", userId);
                // In this system, CartId is often the UserId for logged-in users. 
                // We need to ensure we clear the correct cart.
                // Assuming CartId = UserId for simplicity in this flow as per typical patterns.
                manageCartUseCase.clear(new ClearCartCommand(userId));
            }
        } catch (Exception e) {
            log.error("Failed to process order-created event for cart clearing: {}", message, e);
        }
    }
}
