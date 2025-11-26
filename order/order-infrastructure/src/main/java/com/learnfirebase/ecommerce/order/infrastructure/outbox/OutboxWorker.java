package com.learnfirebase.ecommerce.order.infrastructure.outbox;

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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedDelay = 5000)
    public void publishPending() {
        outboxRepository.findByStatus(OutboxStatus.PENDING).forEach(event -> {
            try {
                kafkaTemplate.send(event.getType(), event.getAggregateId(), event.getPayload()).get();
                event.setStatus(OutboxStatus.PUBLISHED);
                event.setUpdatedAt(java.time.Instant.now());
                outboxRepository.save(event);
            } catch (Exception ex) {
                log.error("Failed to publish outbox event {}", event.getId(), ex);
                event.setStatus(OutboxStatus.FAILED);
                outboxRepository.save(event);
            }
        });
    }

    public void saveRawEvent(String aggregateId, String type, Object payload) {
        try {
            OutboxEntity entity = OutboxEntity.builder()
                .id(UUID.randomUUID().toString())
                .aggregateId(aggregateId)
                .type(type)
                .payload(objectMapper.writeValueAsString(payload))
                .status(OutboxStatus.PENDING)
                .createdAt(java.time.Instant.now())
                .updatedAt(java.time.Instant.now())
                .build();
            outboxRepository.save(entity);
        } catch (Exception e) {
            log.error("Could not serialize outbox payload", e);
        }
    }
}
