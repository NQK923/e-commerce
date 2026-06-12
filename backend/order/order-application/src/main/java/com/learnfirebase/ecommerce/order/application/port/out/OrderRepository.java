package com.learnfirebase.ecommerce.order.application.port.out;

import java.util.List;
import java.util.Optional;

import com.learnfirebase.ecommerce.order.domain.model.Order;
import com.learnfirebase.ecommerce.order.domain.model.OrderId;

public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(OrderId id);

    List<Order> findAll(int page, int size);

    List<Order> findByUserId(String userId, int page, int size);
    
    List<Order> findBySellerId(String sellerId, int page, int size);

    long count();

    long countByUserId(String userId);

    long countBySellerId(String sellerId);
}
