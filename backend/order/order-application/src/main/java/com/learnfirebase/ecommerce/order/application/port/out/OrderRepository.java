package com.learnfirebase.ecommerce.order.application.port.out;

import java.util.Optional;

import com.learnfirebase.ecommerce.order.domain.model.Order;
import com.learnfirebase.ecommerce.order.domain.model.OrderId;

public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(OrderId id);
}
