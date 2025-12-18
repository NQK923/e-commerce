package com.learnfirebase.ecommerce.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpTokenJpaRepository extends JpaRepository<OtpTokenEntity, String> {
}
