package com.learnfirebase.ecommerce.promotion.infrastructure.persistence;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.learnfirebase.ecommerce.promotion.application.port.out.PromotionUsageRepository;

import lombok.RequiredArgsConstructor;

@org.springframework.stereotype.Repository
@RequiredArgsConstructor
public class PromotionUsageRepositoryImpl implements PromotionUsageRepository {
    private final PromotionUsageJpaRepository jpaRepository;

    @Override
    public void recordUsage(String promotionCode, String userId) {
        PromotionUsageEntity entity = PromotionUsageEntity.builder()
                .id(UUID.randomUUID().toString())
                .promotionCode(Objects.requireNonNull(promotionCode))
                .userId(Objects.requireNonNull(userId))
                .usedAt(Instant.now())
                .build();
        jpaRepository.save(entity);
    }
}
