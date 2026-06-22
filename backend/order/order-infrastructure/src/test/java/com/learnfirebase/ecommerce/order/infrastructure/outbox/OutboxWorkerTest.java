package com.learnfirebase.ecommerce.order.infrastructure.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.fasterxml.jackson.databind.ObjectMapper;

class OutboxWorkerTest {

    private OutboxRepository outboxRepository;
    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper;
    private OutboxWorker worker;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        outboxRepository = mock(OutboxRepository.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        worker = new OutboxWorker(outboxRepository, kafkaTemplate, objectMapper);
    }

    @Test
    void saveRawEventSerializesPayloadAndSavesPending() throws Exception {
        TestPayload payload = new TestPayload("123", "user-1");

        worker.saveRawEvent("order-123", "OrderCreated", payload);

        ArgumentCaptor<OutboxEntity> captor = ArgumentCaptor.forClass(OutboxEntity.class);
        verify(outboxRepository).save(captor.capture());

        OutboxEntity saved = captor.getValue();
        assertThat(saved.getAggregateId()).isEqualTo("order-123");
        assertThat(saved.getType()).isEqualTo("OrderCreated");
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(saved.getPayload()).contains("123").contains("user-1");
        assertThat(saved.getAttemptCount()).isEqualTo(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishAsyncSendsEventSuccessfullyAndMarksPublished() throws Exception {
        OutboxEntity event = OutboxEntity.builder()
                .id("event-123")
                .aggregateId("order-123")
                .type("OrderCreated")
                .payload("{\"orderId\":\"123\"}")
                .status(OutboxStatus.PROCESSING)
                .attemptCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(eq("order-events"), eq("order-123"), any(String.class))).thenReturn(future);

        worker.publishAsync(event);

        ArgumentCaptor<OutboxEntity> captor = ArgumentCaptor.forClass(OutboxEntity.class);
        verify(outboxRepository).save(captor.capture());

        OutboxEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishAsyncIncrementsAttemptsAndSetsFailedStatusOnFailure() {
        OutboxEntity event = OutboxEntity.builder()
                .id("event-123")
                .aggregateId("order-123")
                .type("OrderCreated")
                .payload("{\"orderId\":\"123\"}")
                .status(OutboxStatus.PROCESSING)
                .attemptCount(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka connection lost"));
        when(kafkaTemplate.send(eq("order-events"), eq("order-123"), any(String.class))).thenReturn(future);

        worker.publishAsync(event);

        ArgumentCaptor<OutboxEntity> captor = ArgumentCaptor.forClass(OutboxEntity.class);
        verify(outboxRepository).save(captor.capture());

        OutboxEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(saved.getAttemptCount()).isEqualTo(2);
        assertThat(saved.getLastError()).isEqualTo("Kafka connection lost");
        assertThat(saved.getNextRetryAt()).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishAsyncTransitionsToDeadLetterAfterFiveAttempts() {
        OutboxEntity event = OutboxEntity.builder()
                .id("event-123")
                .aggregateId("order-123")
                .type("OrderCreated")
                .payload("{\"orderId\":\"123\"}")
                .status(OutboxStatus.PROCESSING)
                .attemptCount(4) // This will be the 5th attempt
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka broker unavailable"));
        when(kafkaTemplate.send(eq("order-events"), eq("order-123"), any(String.class))).thenReturn(future);

        worker.publishAsync(event);

        ArgumentCaptor<OutboxEntity> captor = ArgumentCaptor.forClass(OutboxEntity.class);
        verify(outboxRepository).save(captor.capture());

        OutboxEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.DEAD_LETTER);
        assertThat(saved.getAttemptCount()).isEqualTo(5);
        assertThat(saved.getLastError()).isEqualTo("Kafka broker unavailable");
        assertThat(saved.getDeadLetterAt()).isNotNull();
    }

    @Test
    void adminOutboxAdapterRetryEventResetsState() {
        OutboxEntity event = OutboxEntity.builder()
                .id("event-123")
                .aggregateId("order-123")
                .type("OrderCreated")
                .payload("{\"orderId\":\"123\"}")
                .status(OutboxStatus.DEAD_LETTER)
                .attemptCount(5)
                .lastError("Kafka broker unavailable")
                .deadLetterAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(outboxRepository.findById("event-123")).thenReturn(Optional.of(event));

        AdminOutboxAdapter adapter = new AdminOutboxAdapter(outboxRepository);
        adapter.retryEvent("event-123");

        ArgumentCaptor<OutboxEntity> captor = ArgumentCaptor.forClass(OutboxEntity.class);
        verify(outboxRepository).save(captor.capture());

        OutboxEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(saved.getAttemptCount()).isEqualTo(0);
        assertThat(saved.getLastError()).isNull();
        assertThat(saved.getDeadLetterAt()).isNull();
        assertThat(saved.getNextRetryAt()).isNull();
    }

    static class TestPayload {
        public String orderId;
        public String userId;

        public TestPayload(String orderId, String userId) {
            this.orderId = orderId;
            this.userId = userId;
        }
    }
}
