package com.learnfirebase.ecommerce.order.infrastructure.outbox;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonRawValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEnvelopeDto {
    private String eventId;
    private String type;
    private String aggregateId;
    private Instant occurredAt;
    private Integer version;

    @JsonRawValue
    private String payload;
}
