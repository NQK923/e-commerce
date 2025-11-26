package com.learnfirebase.ecommerce.cart.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartJpaRepository extends JpaRepository<CartEntity, String> {
}
