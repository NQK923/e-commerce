package com.learnfirebase.ecommerce.order.domain.event;

import java.time.Instant;

import com.learnfirebase.ecommerce.common.domain.DomainEvent;
import com.learnfirebase.ecommerce.common.domain.valueobject.Money;

import lombok.Value;

@Value
public class OrderCreated implements DomainEvent {
    String orderId;
    String userId;
    Instant createdAt;
    Money totalAmount;
}
