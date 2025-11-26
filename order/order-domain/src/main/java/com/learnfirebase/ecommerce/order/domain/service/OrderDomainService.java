package com.learnfirebase.ecommerce.order.domain.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.learnfirebase.ecommerce.common.domain.valueobject.Money;
import com.learnfirebase.ecommerce.order.domain.model.Order;
import com.learnfirebase.ecommerce.order.domain.model.OrderId;
import com.learnfirebase.ecommerce.order.domain.model.OrderItem;
import com.learnfirebase.ecommerce.order.domain.model.UserId;

public class OrderDomainService {
    public Order initiateOrder(UserId userId, List<OrderItem> items, String currency) {
        Money total = items.stream()
            .map(OrderItem::subTotal)
            .reduce(Money.builder().amount(java.math.BigDecimal.ZERO).currency(currency).build(), Money::add);

        Order order = Order.builder()
            .id(new OrderId(UUID.randomUUID().toString()))
            .userId(userId)
            .items(items)
            .totalAmount(total)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        order.markCreated();
        return order;
    }
}
