package com.learnfirebase.ecommerce.order.infrastructure.outbox;

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    PUBLISHED,
    FAILED,
    DEAD_LETTER
}
