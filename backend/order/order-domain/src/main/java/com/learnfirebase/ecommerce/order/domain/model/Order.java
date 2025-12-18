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
    private OrderStatus status = OrderStatus.PENDING;
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
    private Money totalAmount;
    private String trackingNumber;
    private String trackingCarrier;
    private Instant shippedAt;
    private Instant deliveredAt;
    @Builder.Default
    private ReturnStatus returnStatus = ReturnStatus.NONE;
    private String returnReason;
    private String returnNote;
    private Instant returnRequestedAt;
    private Instant returnResolvedAt;
    private Money refundAmount;
    private Instant createdAt;
    private Instant updatedAt;

    public void addItem(OrderItem item) {
        this.items.add(item);
        recalculateTotal();
    }

    public void pay() {
        ensureState(OrderStatus.PENDING);
        this.status = OrderStatus.PAID;
        List<OrderPaid.Item> eventItems = items.stream()
            .map(i -> OrderPaid.Item.builder()
                .productId(i.getProductId())
                .quantity(i.getQuantity())
                .build())
            .toList();
        registerEvent(new OrderPaid(id.getValue(), userId != null ? userId.getValue() : null, Instant.now(), eventItems));
        this.updatedAt = Instant.now();
    }

    public void ship(String trackingNumber, String trackingCarrier) {
        ensureState(OrderStatus.PAID);
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new OrderDomainException("Tracking number is required");
        }
        this.status = OrderStatus.SHIPPING;
        this.trackingNumber = trackingNumber;
        this.trackingCarrier = trackingCarrier;
        this.shippedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void markDelivered() {
        ensureState(OrderStatus.SHIPPING);
        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void requestReturn(String reason, String note) {
        if (this.status != OrderStatus.DELIVERED) {
            throw new OrderDomainException("Only delivered orders can be returned");
        }
        if (this.returnStatus == ReturnStatus.REQUESTED) {
            throw new OrderDomainException("Return already requested");
        }
        if (this.returnStatus == ReturnStatus.APPROVED || this.returnStatus == ReturnStatus.REJECTED) {
            throw new OrderDomainException("Return already resolved");
        }
        this.returnStatus = ReturnStatus.REQUESTED;
        this.returnReason = reason;
        this.returnNote = note;
        this.returnRequestedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void approveReturn(Money refund, String note) {
        ensureReturnState(ReturnStatus.REQUESTED);
        this.status = OrderStatus.RETURNED;
        this.returnStatus = ReturnStatus.APPROVED;
        this.returnNote = note;
        this.refundAmount = refund;
        this.returnResolvedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void rejectReturn(String note) {
        ensureReturnState(ReturnStatus.REQUESTED);
        this.returnStatus = ReturnStatus.REJECTED;
        this.returnNote = note;
        this.returnResolvedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void cancel(String reason) {
        if (this.status == OrderStatus.CANCELLED || this.status == OrderStatus.SHIPPING || this.status == OrderStatus.DELIVERED || this.status == OrderStatus.RETURNED) {
            throw new OrderDomainException("Cannot cancel order in status " + status);
        }
        this.status = OrderStatus.CANCELLED;
        registerEvent(new OrderCancelled(id.getValue(), userId != null ? userId.getValue() : null, reason, Instant.now()));
        this.updatedAt = Instant.now();
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

    private void ensureReturnState(ReturnStatus expected) {
        if (this.returnStatus != expected) {
            throw new OrderDomainException("Return must be in state " + expected + " but is " + returnStatus);
        }
    }
}
