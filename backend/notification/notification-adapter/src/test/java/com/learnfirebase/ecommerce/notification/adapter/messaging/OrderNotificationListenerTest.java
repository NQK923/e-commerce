package com.learnfirebase.ecommerce.notification.adapter.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.learnfirebase.ecommerce.notification.application.command.RecordNotificationCommand;
import com.learnfirebase.ecommerce.notification.application.port.in.RecordNotificationUseCase;
import com.learnfirebase.ecommerce.notification.domain.model.NotificationChannel;

class OrderNotificationListenerTest {

    private RecordNotificationUseCase recordNotificationUseCase;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private OrderNotificationListener listener;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        recordNotificationUseCase = mock(RecordNotificationUseCase.class);
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        listener = new OrderNotificationListener(recordNotificationUseCase, redisTemplate);
    }

    private ConsumerRecord<String, String> createRecord(String message) {
        return new ConsumerRecord<>("order-events", 0, 0L, "key", message);
    }

    @Test
    void handleOrderCreatedSuccessfullySendsNotificationAndMarksDone() {
        String eventId = "event-123";
        String userId = "user-789";
        String orderId = "order-abc";
        String payload = "{\\\"userId\\\":\\\"" + userId + "\\\",\\\"orderId\\\":\\\"" + orderId + "\\\"}";
        String message = "{\"eventId\":\"" + eventId + "\",\"type\":\"OrderCreated\",\"payload\":\"" + payload + "\"}";

        when(valueOperations.setIfAbsent(eq("processed_event:notification:" + eventId), eq("PROCESSING"), any())).thenReturn(true);

        listener.handleOrderEvents(createRecord(message));

        ArgumentCaptor<RecordNotificationCommand> commandCaptor = ArgumentCaptor.forClass(RecordNotificationCommand.class);
        verify(recordNotificationUseCase).record(commandCaptor.capture());
        verify(valueOperations).set(eq("processed_event:notification:" + eventId), eq("DONE"), any());

        RecordNotificationCommand command = commandCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(userId, command.getUserId());
        org.junit.jupiter.api.Assertions.assertEquals("Order created", command.getTitle());
        org.junit.jupiter.api.Assertions.assertEquals("Order " + orderId + " has been created.", command.getBody());
        org.junit.jupiter.api.Assertions.assertEquals(NotificationChannel.PUSH, command.getChannel());
    }

    @Test
    void handleOrderPaidSuccessfullySendsNotificationAndMarksDone() {
        String eventId = "event-456";
        String userId = "user-789";
        String orderId = "order-def";
        String payload = "{\\\"userId\\\":\\\"" + userId + "\\\",\\\"orderId\\\":\\\"" + orderId + "\\\"}";
        String message = "{\"eventId\":\"" + eventId + "\",\"type\":\"OrderPaid\",\"payload\":\"" + payload + "\"}";

        when(valueOperations.setIfAbsent(eq("processed_event:notification:" + eventId), eq("PROCESSING"), any())).thenReturn(true);

        listener.handleOrderEvents(createRecord(message));

        ArgumentCaptor<RecordNotificationCommand> commandCaptor = ArgumentCaptor.forClass(RecordNotificationCommand.class);
        verify(recordNotificationUseCase).record(commandCaptor.capture());
        verify(valueOperations).set(eq("processed_event:notification:" + eventId), eq("DONE"), any());

        RecordNotificationCommand command = commandCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(userId, command.getUserId());
        org.junit.jupiter.api.Assertions.assertEquals("Order paid", command.getTitle());
        org.junit.jupiter.api.Assertions.assertEquals("Order " + orderId + " was paid successfully.", command.getBody());
    }

    @Test
    void handleOrderCancelledSuccessfullySendsNotificationAndMarksDone() {
        String eventId = "event-789";
        String userId = "user-789";
        String orderId = "order-ghi";
        String payload = "{\\\"userId\\\":\\\"" + userId + "\\\",\\\"orderId\\\":\\\"" + orderId + "\\\"}";
        String message = "{\"eventId\":\"" + eventId + "\",\"type\":\"OrderCancelled\",\"payload\":\"" + payload + "\"}";

        when(valueOperations.setIfAbsent(eq("processed_event:notification:" + eventId), eq("PROCESSING"), any())).thenReturn(true);

        listener.handleOrderEvents(createRecord(message));

        ArgumentCaptor<RecordNotificationCommand> commandCaptor = ArgumentCaptor.forClass(RecordNotificationCommand.class);
        verify(recordNotificationUseCase).record(commandCaptor.capture());
        verify(valueOperations).set(eq("processed_event:notification:" + eventId), eq("DONE"), any());

        RecordNotificationCommand command = commandCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(userId, command.getUserId());
        org.junit.jupiter.api.Assertions.assertEquals("Order cancelled", command.getTitle());
        org.junit.jupiter.api.Assertions.assertEquals("Order " + orderId + " has been cancelled.", command.getBody());
    }

    @Test
    void handleOrderEventsSkipsDuplicateEvent() {
        String eventId = "event-123";
        String message = "{\"eventId\":\"" + eventId + "\",\"type\":\"OrderCreated\",\"payload\":\"{\\\"userId\\\":\\\"user-123\\\"}\"}";

        when(valueOperations.setIfAbsent(eq("processed_event:notification:" + eventId), eq("PROCESSING"), any())).thenReturn(false);

        listener.handleOrderEvents(createRecord(message));

        verify(recordNotificationUseCase, never()).record(any());
        verify(valueOperations, never()).set(any(), any(), any());
    }

    @Test
    void handleOrderEventsDeletesIdempotencyKeyAndRethrowsOnException() {
        String eventId = "event-123";
        String message = "{\"eventId\":\"" + eventId + "\",\"type\":\"OrderCreated\",\"payload\":\"{\\\"userId\\\":\\\"user-123\\\",\\\"orderId\\\":\\\"order-abc\\\"}\"}";

        when(valueOperations.setIfAbsent(eq("processed_event:notification:" + eventId), eq("PROCESSING"), any())).thenReturn(true);
        when(recordNotificationUseCase.record(any())).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> listener.handleOrderEvents(createRecord(message)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error processing event " + eventId);

        verify(redisTemplate).delete("processed_event:notification:" + eventId);
        verify(valueOperations, never()).set(eq("processed_event:notification:" + eventId), eq("DONE"), any());
    }

    @Test
    void handleOrderEventsLogsAndIgnoresMalformedMessage() {
        String message = "invalid json";

        listener.handleOrderEvents(createRecord(message));

        verify(redisTemplate, never()).opsForValue();
        verify(recordNotificationUseCase, never()).record(any());
    }

    @Test
    void handleOrderEventsSkipsNotificationWhenUserIdIsMissing() {
        String eventId = "event-123";
        String message = "{\"eventId\":\"" + eventId + "\",\"type\":\"OrderCreated\",\"payload\":\"{\\\"orderId\\\":\\\"order-abc\\\"}\"}";

        when(valueOperations.setIfAbsent(eq("processed_event:notification:" + eventId), eq("PROCESSING"), any())).thenReturn(true);

        listener.handleOrderEvents(createRecord(message));

        verify(recordNotificationUseCase, never()).record(any());
        verify(valueOperations).set(eq("processed_event:notification:" + eventId), eq("DONE"), any());
    }

    @Test
    void handleOrderEventsSkipsNotificationWhenTypeIsUnrecognized() {
        String eventId = "event-123";
        String message = "{\"eventId\":\"" + eventId + "\",\"type\":\"UnknownType\",\"payload\":\"{\\\"userId\\\":\\\"user-123\\\",\\\"orderId\\\":\\\"order-abc\\\"}\"}";

        when(valueOperations.setIfAbsent(eq("processed_event:notification:" + eventId), eq("PROCESSING"), any())).thenReturn(true);

        listener.handleOrderEvents(createRecord(message));

        verify(recordNotificationUseCase, never()).record(any());
        verify(valueOperations).set(eq("processed_event:notification:" + eventId), eq("DONE"), any());
    }
}
