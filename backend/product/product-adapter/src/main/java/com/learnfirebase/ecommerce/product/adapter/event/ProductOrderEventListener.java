package com.learnfirebase.ecommerce.product.adapter.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.order.domain.event.OrderPaid;
import com.learnfirebase.ecommerce.product.application.dto.SoldItemDto;
import com.learnfirebase.ecommerce.product.application.service.ProductApplicationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductOrderEventListener {
    private final ProductApplicationService productApplicationService;

    @Async
    @EventListener
    public void handleOrderPaid(OrderPaid event) {
        log.info("Received OrderPaid event for order: {}", event.getOrderId());
        if (event.getItems() != null && !event.getItems().isEmpty()) {
            var soldItems = event.getItems().stream()
                .map(item -> SoldItemDto.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .build())
                .toList();
            productApplicationService.handleOrderPaid(soldItems);
        } else {
            log.warn("OrderPaid event has no items for order: {}", event.getOrderId());
        }
    }
}
