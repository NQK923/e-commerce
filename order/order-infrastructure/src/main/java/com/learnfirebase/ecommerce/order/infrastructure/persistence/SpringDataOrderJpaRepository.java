package com.learnfirebase.ecommerce.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOrderJpaRepository extends JpaRepository<JpaOrderEntity, String> {
}
