package com.learnfirebase.ecommerce.identity.infrastructure.persistence;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.learnfirebase.ecommerce.identity.application.port.out.OtpRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OtpRepositoryAdapter implements OtpRepository {
    private final OtpTokenJpaRepository jpaRepository;

    @Override
    public String save(String email, String userId, String codeHash, Instant expiresAt) {
        String id = UUID.randomUUID().toString();
        OtpTokenEntity entity = OtpTokenEntity.builder()
                .id(id)
                .email(email)
                .userId(userId)
                .codeHash(codeHash)
                .expiresAt(expiresAt)
                .used(false)
                .attempts(0)
                .createdAt(Instant.now())
                .build();
        jpaRepository.save(Objects.requireNonNull(entity));
        return id;
    }

    @Override
    public Optional<OtpRecord> findById(String id) {
        return jpaRepository.findById(Objects.requireNonNull(id))
                .map(e -> new OtpRecord(e.getId(), e.getEmail(), e.getUserId(), e.getCodeHash(), e.getExpiresAt(),
                        e.isUsed(), e.getAttempts()));
    }

    @Transactional
    @Override
    public void markUsed(String id) {
        jpaRepository.findById(Objects.requireNonNull(id)).ifPresent(entity -> {
            entity.setUsed(true);
            jpaRepository.save(entity);
        });
    }

    @Transactional
    @Override
    public void incrementAttempts(String id) {
        jpaRepository.findById(Objects.requireNonNull(id)).ifPresent(entity -> {
            entity.setAttempts(entity.getAttempts() + 1);
            jpaRepository.save(entity);
        });
    }
}
