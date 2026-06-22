package com.learnfirebase.ecommerce.order.application.port.in;

public interface RetryOutboxEventUseCase {
    void retryEvent(String eventId);
}
