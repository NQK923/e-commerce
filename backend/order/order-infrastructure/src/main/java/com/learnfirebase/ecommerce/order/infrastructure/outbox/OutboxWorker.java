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

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxWorker {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    public void publishPending() {
        outboxRepository.findByStatus(OutboxStatus.PENDING).forEach(this::publishAsync);
    }

    private void publishAsync(OutboxEntity event) {
        kafkaTemplate
                .send(Objects.requireNonNull(event.getType()), Objects.requireNonNull(event.getAggregateId()),
                        Objects.requireNonNull(event.getPayload()))
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        markPublished(event);
                    } else {
                        markFailed(event, ex);
                    }
                });
    }

    private void markPublished(OutboxEntity event) {
        event.setStatus(OutboxStatus.PUBLISHED);
        event.setUpdatedAt(Instant.now());
        outboxRepository.save(event);
    }

    private void markFailed(OutboxEntity event, Throwable ex) {
        log.error("Failed to publish outbox event {}", event.getId(), ex);
        event.setStatus(OutboxStatus.FAILED);
        event.setUpdatedAt(Instant.now());
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
                    .build();
            outboxRepository.save(Objects.requireNonNull(entity));
        } catch (Exception e) {
            log.error("Could not serialize outbox payload", e);
        }
    }
}
