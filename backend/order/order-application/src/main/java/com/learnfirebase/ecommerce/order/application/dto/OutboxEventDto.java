package com.learnfirebase.ecommerce.order.application.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEventDto {
    private String id;
    private String aggregateId;
    private String type;
    private String payload;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private int attemptCount;
    private String lastError;
    private Instant nextRetryAt;
    private Instant deadLetterAt;
}
