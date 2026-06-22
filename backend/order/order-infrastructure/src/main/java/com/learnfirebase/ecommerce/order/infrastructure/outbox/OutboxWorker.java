package com.learnfirebase.ecommerce.order.infrastructure.outbox;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxWorker {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPending() {
        List<OutboxEntity> pending = outboxRepository.findAndLockByStatus(OutboxStatus.PENDING.name(), 50);
        if (pending.isEmpty()) return;

        pending.forEach(event -> {
            event.setStatus(OutboxStatus.PROCESSING);
            event.setUpdatedAt(Instant.now());
        });
        outboxRepository.saveAll(pending);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                pending.forEach(OutboxWorker.this::publishAsync);
            }
        });
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void retryFailedAndStuck() {
        Instant now = Instant.now();
        List<OutboxEntity> failed = outboxRepository.findAndLockByStatus(OutboxStatus.FAILED.name(), 50);
        List<OutboxEntity> stuck = outboxRepository.findAndLockByStatus(OutboxStatus.PROCESSING.name(), 50);
        
        List<OutboxEntity> toRetry = new java.util.ArrayList<>();

        failed.forEach(event -> {
            if (event.getNextRetryAt() == null || !now.isBefore(event.getNextRetryAt())) {
                toRetry.add(event);
            }
        });

        // Recover events that have been in PROCESSING for more than 5 minutes (stuck due to crash)
        stuck.forEach(event -> {
            if (event.getUpdatedAt().isBefore(now.minusSeconds(300))) {
                toRetry.add(event);
            }
        });

        if (toRetry.isEmpty()) return;

        toRetry.forEach(event -> {
            event.setStatus(OutboxStatus.PROCESSING);
            event.setUpdatedAt(now);
        });
        outboxRepository.saveAll(toRetry);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                toRetry.forEach(OutboxWorker.this::publishAsync);
            }
        });
    }

    void publishAsync(OutboxEntity event) {
        try {
            EventEnvelopeDto envelope = EventEnvelopeDto.builder()
                    .eventId(event.getId())
                    .type(event.getType())
                    .aggregateId(event.getAggregateId())
                    .occurredAt(event.getCreatedAt())
                    .version(1)
                    .payload(event.getPayload())
                    .build();

            String message = objectMapper.writeValueAsString(envelope);
            log.info("[OUTBOX-PUBLISH] Outbox event {} of type {} for aggregate {} is being published", event.getId(), event.getType(), event.getAggregateId());
            kafkaTemplate
                    .send("order-events", Objects.requireNonNull(event.getAggregateId()), message)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            markPublished(event);
                        } else {
                            markFailed(event, ex);
                        }
                    });
        } catch (Exception ex) {
            markFailed(event, ex);
        }
    }

    private void markPublished(OutboxEntity event) {
        log.info("[OUTBOX-PUBLISH-SUCCESS] Outbox event {} successfully published to Kafka. Aggregate: {}", event.getId(), event.getAggregateId());
        event.setStatus(OutboxStatus.PUBLISHED);
        event.setUpdatedAt(Instant.now());
        outboxRepository.save(event);
    }

    private void markFailed(OutboxEntity event, Throwable ex) {
        log.error("[OUTBOX-PUBLISH-ERROR] Outbox event {} failed to publish. Attempt: {}, Status: {}. Error: {}", event.getId(), event.getAttemptCount() + 1, event.getStatus(), ex.getMessage(), ex);
        int newAttemptCount = event.getAttemptCount() + 1;
        event.setAttemptCount(newAttemptCount);
        event.setLastError(ex.getMessage());
        event.setUpdatedAt(Instant.now());

        if (newAttemptCount >= 5) {
            event.setStatus(OutboxStatus.DEAD_LETTER);
            event.setDeadLetterAt(Instant.now());
        } else {
            event.setStatus(OutboxStatus.FAILED);
            event.setNextRetryAt(Instant.now().plusSeconds((long) Math.pow(2, newAttemptCount) * 10)); // Exponential backoff: 20s, 40s, 80s...
        }
        
        outboxRepository.save(event);
    }

    public void saveRawEvent(String aggregateId, String type, Object payload) {
        try {
            OutboxEntity entity = OutboxEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateId(aggregateId)
                    .type(type)
                    .payload(objectMapper.writeValueAsString(payload))
                    .status(OutboxStatus.PENDING)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .attemptCount(0)
                    .build();
            outboxRepository.save(Objects.requireNonNull(entity));
        } catch (Exception e) {
            log.error("Could not serialize outbox payload", e);
        }
    }
}
