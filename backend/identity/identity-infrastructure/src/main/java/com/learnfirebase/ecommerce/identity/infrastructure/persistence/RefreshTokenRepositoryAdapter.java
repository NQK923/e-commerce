package com.learnfirebase.ecommerce.identity.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.learnfirebase.ecommerce.identity.application.port.out.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {
    private final RefreshTokenJpaRepository jpaRepository;

    @Override
    public void save(String tokenId, String userId, String email, String deviceId, String tokenHash, Instant expiresAt) {
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
            .id(tokenId != null ? tokenId : UUID.randomUUID().toString())
            .userId(userId)
            .email(email)
            .deviceId(deviceId)
            .tokenHash(tokenHash)
            .expiresAt(expiresAt)
            .revoked(false)
            .createdAt(Instant.now())
            .build();
        jpaRepository.save(entity);
    }

    @Override
    public Optional<RefreshTokenRecord> findActiveByToken(String tokenHash) {
        return jpaRepository.findByTokenHashAndRevokedFalse(tokenHash)
            .map(e -> new RefreshTokenRecord(e.getId(), e.getUserId(), e.getEmail(), e.getDeviceId(), e.getTokenHash(), e.getExpiresAt(), e.isRevoked()));
    }

    @Transactional
    @Override
    public void revoke(String tokenId) {
        jpaRepository.findById(tokenId).ifPresent(entity -> {
            entity.setRevoked(true);
            jpaRepository.save(entity);
        });
    }
}
