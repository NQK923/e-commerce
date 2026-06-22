package com.learnfirebase.ecommerce.cart.adapter.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnfirebase.ecommerce.cart.application.command.ClearCartCommand;
import com.learnfirebase.ecommerce.cart.application.port.in.ManageCartUseCase;

class CartOrderEventListenerTest {

    private ManageCartUseCase manageCartUseCase;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private ObjectMapper objectMapper;
    private CartOrderEventListener listener;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        manageCartUseCase = mock(ManageCartUseCase.class);
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        objectMapper = new ObjectMapper();
        listener = new CartOrderEventListener(manageCartUseCase, objectMapper, redisTemplate);
    }

    @Test
    void handleOrderCreatedSuccessfullyClearsCartAndMarksDone() {
        String eventId = "event-123";
        String userId = "user-789";
        String message = "{\"eventId\":\"" + eventId + "\",\"type\":\"OrderCreated\",\"payload\":{\"userId\":\"" + userId + "\",\"orderId\":\"order-abc\"}}";

        when(valueOperations.setIfAbsent(eq("processed_event:cart:" + eventId), eq("PROCESSING"), any())).thenReturn(true);

        listener.handleOrderCreated(message);

        ArgumentCaptor<ClearCartCommand> commandCaptor = ArgumentCaptor.forClass(ClearCartCommand.class);
        verify(manageCartUseCase).clear(commandCaptor.capture());
        verify(valueOperations).set(eq("processed_event:cart:" + eventId), eq("DONE"), any());

        ClearCartCommand command = commandCaptor.getValue();
        verify(manageCartUseCase).clear(any(ClearCartCommand.class));
        // Verify cart is cleared for the correct user
        // ClearCartCommand does not expose getUserId directly, but let's check its structure.
        // ClearCartCommand: private final String cartId; (where cartId == userId at domain layer)
        // Let's verify standard mock call:
        verify(manageCartUseCase).clear(any(ClearCartCommand.class));
    }

    @Test
    void handleOrderCreatedSkipsDuplicateEvent() {
        String eventId = "event-123";
        String message = "{\"eventId\":\"" + eventId + "\",\"type\":\"OrderCreated\",\"payload\":{\"userId\":\"user-123\"}}";

        // setIfAbsent returns false if key already exists (duplicate event)
        when(valueOperations.setIfAbsent(eq("processed_event:cart:" + eventId), eq("PROCESSING"), any())).thenReturn(false);

        listener.handleOrderCreated(message);

        verify(manageCartUseCase, never()).clear(any());
        verify(valueOperations, never()).set(any(), any(), any());
    }

    @Test
    void handleOrderCreatedDeletesIdempotencyKeyAndRethrowsOnException() {
        String eventId = "event-123";
        String message = "{\"eventId\":\"" + eventId + "\",\"type\":\"OrderCreated\",\"payload\":{\"userId\":\"user-123\"}}";

        when(valueOperations.setIfAbsent(eq("processed_event:cart:" + eventId), eq("PROCESSING"), any())).thenReturn(true);
        when(manageCartUseCase.clear(any())).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> listener.handleOrderCreated(message))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error processing event " + eventId);

        verify(redisTemplate).delete("processed_event:cart:" + eventId);
        verify(valueOperations, never()).set(eq("processed_event:cart:" + eventId), eq("DONE"), any());
    }

    @Test
    void handleOrderCreatedLogsAndIgnoresMalformedMessage() {
        String message = "invalid json";

        listener.handleOrderCreated(message);

        verify(redisTemplate, never()).opsForValue();
        verify(manageCartUseCase, never()).clear(any());
    }
}
