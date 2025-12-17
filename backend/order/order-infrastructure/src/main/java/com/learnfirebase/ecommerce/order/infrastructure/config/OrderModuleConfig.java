package com.learnfirebase.ecommerce.order.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.learnfirebase.ecommerce.order.application.port.out.InventoryReservationPort;
import com.learnfirebase.ecommerce.order.application.port.out.LoadProductPort;
import com.learnfirebase.ecommerce.order.application.port.out.OrderEventPublisher;
import com.learnfirebase.ecommerce.order.application.port.out.OrderOutboxPort;
import com.learnfirebase.ecommerce.order.application.port.out.OrderRepository;
import com.learnfirebase.ecommerce.order.application.port.out.PaymentGatewayPort;
import com.learnfirebase.ecommerce.order.application.port.out.PaymentTransactionPort;
import com.learnfirebase.ecommerce.order.application.service.OrderApplicationService;
import com.learnfirebase.ecommerce.order.infrastructure.outbox.OutboxEntity;
import com.learnfirebase.ecommerce.order.infrastructure.outbox.OutboxRepository;
import com.learnfirebase.ecommerce.order.infrastructure.payment.PaymentTransactionEntity;
import com.learnfirebase.ecommerce.order.infrastructure.payment.PaymentTransactionJpaRepository;
import com.learnfirebase.ecommerce.order.infrastructure.payment.VnPayProperties;
import com.learnfirebase.ecommerce.order.infrastructure.persistence.JpaOrderEntity;
import com.learnfirebase.ecommerce.order.infrastructure.persistence.SpringDataOrderJpaRepository;

@Configuration
@EnableJpaRepositories(basePackageClasses = {SpringDataOrderJpaRepository.class, OutboxRepository.class, PaymentTransactionJpaRepository.class})
@EntityScan(basePackageClasses = {JpaOrderEntity.class, OutboxEntity.class, PaymentTransactionEntity.class})
@EnableConfigurationProperties(VnPayProperties.class)
public class OrderModuleConfig {
    @Bean
    public OrderApplicationService orderApplicationService(
        OrderRepository orderRepository,
        LoadProductPort loadProductPort,
        com.learnfirebase.ecommerce.order.application.port.out.LoadFlashSalePort loadFlashSalePort,
        InventoryReservationPort inventoryReservationPort,
        OrderOutboxPort orderOutboxPort,
        OrderEventPublisher orderEventPublisher,
        PaymentGatewayPort paymentGatewayPort,
        PaymentTransactionPort paymentTransactionPort
    ) {
        return new OrderApplicationService(orderRepository, loadProductPort, loadFlashSalePort, inventoryReservationPort, orderOutboxPort, orderEventPublisher, paymentGatewayPort, paymentTransactionPort);
    }
}
