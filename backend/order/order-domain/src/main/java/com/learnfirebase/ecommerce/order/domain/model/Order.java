package com.learnfirebase.ecommerce.order.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.learnfirebase.ecommerce.common.domain.AggregateRoot;
import com.learnfirebase.ecommerce.common.domain.valueobject.Money;
import com.learnfirebase.ecommerce.order.domain.event.OrderCancelled;
import com.learnfirebase.ecommerce.order.domain.event.OrderCreated;
import com.learnfirebase.ecommerce.order.domain.event.OrderPaid;
import com.learnfirebase.ecommerce.order.domain.exception.OrderDomainException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class Order extends AggregateRoot<OrderId> {
    private OrderId id;
    private UserId userId;
    @Builder.Default
    private OrderStatus status = OrderStatus.CREATED;
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
    private Money totalAmount;
    private Instant createdAt;
    private Instant updatedAt;

    public void addItem(OrderItem item) {
        this.items.add(item);
        recalculateTotal();
    }

    public void confirm() {
        ensureState(OrderStatus.CREATED);
        this.status = OrderStatus.CONFIRMED;
    }

    public void pay() {
        ensureState(OrderStatus.CONFIRMED);
        this.status = OrderStatus.PAID;
        registerEvent(new OrderPaid(id.getValue(), Instant.now()));
    }

    public void cancel(String reason) {
        if (this.status == OrderStatus.CANCELLED || this.status == OrderStatus.COMPLETED) {
            throw new OrderDomainException("Cannot cancel order in status " + status);
        }
        this.status = OrderStatus.CANCELLED;
        registerEvent(new OrderCancelled(id.getValue(), reason, Instant.now()));
    }

    public void complete() {
        ensureState(OrderStatus.PAID);
        this.status = OrderStatus.COMPLETED;
    }

    public void markCreated() {
        registerEvent(new OrderCreated(id.getValue(), userId.getValue(), Instant.now(), totalAmount));
    }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
            .map(OrderItem::subTotal)
            .reduce(Money::add)
            .orElse(Money.builder().amount(java.math.BigDecimal.ZERO).currency(defaultCurrency()).build());
        this.updatedAt = Instant.now();
    }

    private String defaultCurrency() {
        return totalAmount != null ? totalAmount.getCurrency() : "USD";
    }

    private void ensureState(OrderStatus expected) {
        if (this.status != expected) {
            throw new OrderDomainException("Order must be in state " + expected + " but is " + status);
        }
    }
}
