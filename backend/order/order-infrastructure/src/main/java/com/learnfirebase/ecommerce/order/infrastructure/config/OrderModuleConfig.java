package com.learnfirebase.ecommerce.order.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.learnfirebase.ecommerce.order.application.port.out.InventoryReservationPort;
import com.learnfirebase.ecommerce.order.application.port.out.LoadProductPort;
import com.learnfirebase.ecommerce.order.application.port.out.OrderEventPublisher;
import com.learnfirebase.ecommerce.order.application.port.out.OrderOutboxPort;
import com.learnfirebase.ecommerce.order.application.port.out.OrderRepository;
import com.learnfirebase.ecommerce.order.application.service.OrderApplicationService;
import com.learnfirebase.ecommerce.order.infrastructure.outbox.OutboxEntity;
import com.learnfirebase.ecommerce.order.infrastructure.outbox.OutboxRepository;
import com.learnfirebase.ecommerce.order.infrastructure.persistence.JpaOrderEntity;
import com.learnfirebase.ecommerce.order.infrastructure.persistence.SpringDataOrderJpaRepository;

@Configuration
@EnableJpaRepositories(basePackageClasses = {SpringDataOrderJpaRepository.class, OutboxRepository.class})
@EntityScan(basePackageClasses = {JpaOrderEntity.class, OutboxEntity.class})
public class OrderModuleConfig {
    @Bean
    public OrderApplicationService orderApplicationService(
        OrderRepository orderRepository,
        LoadProductPort loadProductPort,
        com.learnfirebase.ecommerce.order.application.port.out.LoadFlashSalePort loadFlashSalePort,
        InventoryReservationPort inventoryReservationPort,
        OrderOutboxPort orderOutboxPort,
        OrderEventPublisher orderEventPublisher
    ) {
        return new OrderApplicationService(orderRepository, loadProductPort, loadFlashSalePort, inventoryReservationPort, orderOutboxPort, orderEventPublisher);
    }
}
