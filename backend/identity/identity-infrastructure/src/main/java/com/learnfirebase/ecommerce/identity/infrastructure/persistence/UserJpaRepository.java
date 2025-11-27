package com.learnfirebase.ecommerce.identity.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnfirebase.ecommerce.identity.domain.model.AuthProvider;

public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByAuthProviderAndProviderUserId(AuthProvider authProvider, String providerUserId);
}
