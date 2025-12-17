package com.learnfirebase.ecommerce.order.application.model;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentRecord {
    String reference;
    String orderId;
    String gateway;
    BigDecimal amount;
    String currency;
    PaymentStatus status;
    String transactionNo;
    String rawPayload;
    Instant createdAt;
    Instant updatedAt;
}
