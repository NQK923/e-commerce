package com.learnfirebase.ecommerce.order.infrastructure.payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionJpaRepository extends JpaRepository<PaymentTransactionEntity, String> {
    Optional<PaymentTransactionEntity> findByOrderId(String orderId);
}
