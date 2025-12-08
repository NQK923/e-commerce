package com.learnfirebase.ecommerce.order.domain.event;

import java.time.Instant;
import java.util.List;

import com.learnfirebase.ecommerce.common.domain.DomainEvent;

import lombok.Builder;
import lombok.Value;

@Value
public class OrderPaid implements DomainEvent {
    String orderId;
    Instant paidAt;
    List<Item> items;

    @Value
    @Builder
    public static class Item {
        String productId;
        int quantity;
    }
}
