package com.learnfirebase.ecommerce.order.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.learnfirebase.ecommerce.order.application.port.out.InventoryReservationPort;
import com.learnfirebase.ecommerce.order.application.port.out.LoadProductPort;
import com.learnfirebase.ecommerce.order.application.port.out.OrderEventPublisher;
import com.learnfirebase.ecommerce.order.application.port.out.OrderOutboxPort;
import com.learnfirebase.ecommerce.order.application.port.out.OrderRepository;
import com.learnfirebase.ecommerce.order.application.service.OrderApplicationService;

@Configuration
public class OrderModuleConfig {
    @Bean
    public OrderApplicationService orderApplicationService(
        OrderRepository orderRepository,
        LoadProductPort loadProductPort,
        InventoryReservationPort inventoryReservationPort,
        OrderOutboxPort orderOutboxPort,
        OrderEventPublisher orderEventPublisher
    ) {
        return new OrderApplicationService(orderRepository, loadProductPort, inventoryReservationPort, orderOutboxPort, orderEventPublisher);
    }
}
