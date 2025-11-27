package com.learnfirebase.ecommerce.order.domain.event;

import java.time.Instant;

import com.learnfirebase.ecommerce.common.domain.DomainEvent;

import lombok.Value;

@Value
public class OrderPaid implements DomainEvent {
    String orderId;
    Instant paidAt;
}
