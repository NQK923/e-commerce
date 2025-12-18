package com.learnfirebase.ecommerce.order.application.dto;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class OrderDto {
    String id;
    String userId;
    String status;
    String currency;
    String totalAmount;
    String trackingNumber;
    String trackingCarrier;
    Instant shippedAt;
    Instant deliveredAt;
    String returnStatus;
    String returnReason;
    String returnNote;
    Instant returnRequestedAt;
    Instant returnResolvedAt;
    String refundAmount;
    Instant createdAt;
    Instant updatedAt;
    @Singular
    List<OrderItemDto> items;

    @Value
    @Builder
    public static class OrderItemDto {
        String productId;
        String variantSku;
        String flashSaleId;
        int quantity;
        String price;
    }
}
