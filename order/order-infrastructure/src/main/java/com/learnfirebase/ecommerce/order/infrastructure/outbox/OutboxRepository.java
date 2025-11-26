package com.learnfirebase.ecommerce.order.infrastructure.outbox;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<OutboxEntity, String> {
    List<OutboxEntity> findByStatus(OutboxStatus status);
}
