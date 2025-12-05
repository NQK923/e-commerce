package com.learnfirebase.ecommerce.order.application.port.in;

import com.learnfirebase.ecommerce.order.application.dto.OrderDto;

public interface GetOrderUseCase {
    OrderDto getOrder(String orderId);
}
