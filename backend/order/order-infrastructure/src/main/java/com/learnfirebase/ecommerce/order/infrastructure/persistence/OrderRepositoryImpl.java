package com.learnfirebase.ecommerce.order.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.learnfirebase.ecommerce.common.domain.valueobject.Money;
import com.learnfirebase.ecommerce.order.application.port.out.OrderRepository;
import com.learnfirebase.ecommerce.order.domain.model.Order;
import com.learnfirebase.ecommerce.order.domain.model.OrderId;
import com.learnfirebase.ecommerce.order.domain.model.OrderItem;
import com.learnfirebase.ecommerce.order.domain.model.OrderStatus;
import com.learnfirebase.ecommerce.order.domain.model.ReturnStatus;
import com.learnfirebase.ecommerce.order.domain.model.UserId;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    private final SpringDataOrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        JpaOrderEntity entity = toEntity(order);
        JpaOrderEntity saved = orderJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return orderJpaRepository.findById(id.getValue()).map(this::toDomain);
    }

    @Override
    public List<Order> findAll(int page, int size) {
        return orderJpaRepository.findAll(PageRequest.of(page, size)).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Order> findBySellerId(String sellerId, int page, int size) {
        return orderJpaRepository.findBySellerId(sellerId, PageRequest.of(page, size)).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return orderJpaRepository.count();
    }

    @Override
    public long countBySellerId(String sellerId) {
        return orderJpaRepository.countBySellerId(sellerId);
    }

    private JpaOrderEntity toEntity(Order order) {
        JpaOrderEntity entity = JpaOrderEntity.builder()
            .id(order.getId().getValue())
            .userId(order.getUserId().getValue())
            .status(order.getStatus().name())
            .currency(order.getTotalAmount().getCurrency())
            .totalAmount(order.getTotalAmount().getAmount().toPlainString())
            .trackingNumber(order.getTrackingNumber())
            .trackingCarrier(order.getTrackingCarrier())
            .shippedAt(order.getShippedAt())
            .deliveredAt(order.getDeliveredAt())
            .returnStatus(order.getReturnStatus() != null ? order.getReturnStatus().name() : null)
            .returnReason(order.getReturnReason())
            .returnNote(order.getReturnNote())
            .returnRequestedAt(order.getReturnRequestedAt())
            .returnResolvedAt(order.getReturnResolvedAt())
            .refundAmount(order.getRefundAmount() != null ? order.getRefundAmount().getAmount().toPlainString() : null)
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
        entity.setItems(order.getItems().stream().map(item -> JpaOrderItemEntity.builder()
            .id(UUID.randomUUID().toString())
            .productId(item.getProductId())
            .variantSku(item.getVariantSku())
            .flashSaleId(item.getFlashSaleId())
            .sellerId(item.getSellerId())
            .quantity(item.getQuantity())
            .price(item.getPrice().getAmount().toPlainString())
            .order(entity)
            .build()).collect(Collectors.toList()));
        return entity;
    }

    private Order toDomain(JpaOrderEntity entity) {
        return Order.builder()
            .id(new OrderId(entity.getId()))
            .userId(new UserId(entity.getUserId()))
            .status(mapStatus(entity.getStatus()))
            .items(entity.getItems().stream()
                .map(item -> OrderItem.builder()
                    .productId(item.getProductId())
                    .variantSku(item.getVariantSku())
                    .flashSaleId(item.getFlashSaleId())
                    .sellerId(item.getSellerId())
                    .quantity(item.getQuantity())
                    .price(Money.builder().amount(new java.math.BigDecimal(item.getPrice())).currency(entity.getCurrency()).build())
                    .build())
                .collect(Collectors.toList()))
            .totalAmount(Money.builder().amount(new java.math.BigDecimal(entity.getTotalAmount())).currency(entity.getCurrency()).build())
            .trackingNumber(entity.getTrackingNumber())
            .trackingCarrier(entity.getTrackingCarrier())
            .shippedAt(entity.getShippedAt())
            .deliveredAt(entity.getDeliveredAt())
            .returnStatus(entity.getReturnStatus() != null ? ReturnStatus.valueOf(entity.getReturnStatus()) : ReturnStatus.NONE)
            .returnReason(entity.getReturnReason())
            .returnNote(entity.getReturnNote())
            .returnRequestedAt(entity.getReturnRequestedAt())
            .returnResolvedAt(entity.getReturnResolvedAt())
            .refundAmount(entity.getRefundAmount() != null ? Money.builder().amount(new java.math.BigDecimal(entity.getRefundAmount())).currency(entity.getCurrency()).build() : null)
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    private OrderStatus mapStatus(String status) {
        if (status == null) {
            return OrderStatus.PENDING;
        }
        return switch (status) {
            case "CREATED", "CONFIRMED" -> OrderStatus.PENDING;
            case "COMPLETED" -> OrderStatus.DELIVERED;
            default -> OrderStatus.valueOf(status);
        };
    }
}
