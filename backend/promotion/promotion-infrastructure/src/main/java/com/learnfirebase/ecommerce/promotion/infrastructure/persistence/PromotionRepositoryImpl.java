package com.learnfirebase.ecommerce.promotion.infrastructure.persistence;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.learnfirebase.ecommerce.promotion.application.port.out.PromotionRepository;
import com.learnfirebase.ecommerce.promotion.domain.model.Promotion;
import com.learnfirebase.ecommerce.promotion.domain.model.PromotionId;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PromotionRepositoryImpl implements PromotionRepository {
    private final PromotionJpaRepository promotionJpaRepository;

    @Override
    public Optional<Promotion> findByCode(String code) {
        return promotionJpaRepository.findByCode(Objects.requireNonNull(code)).map(this::toDomain);
    }

    @Override
    public Promotion save(Promotion promotion) {
        PromotionEntity entity = toEntity(promotion);
        PromotionEntity saved = promotionJpaRepository.save(Objects.requireNonNull(entity));
        return toDomain(saved);
    }

    @Override
    public Optional<Promotion> findById(PromotionId id) {
        return promotionJpaRepository.findById(Objects.requireNonNull(id.getValue())).map(this::toDomain);
    }

    private PromotionEntity toEntity(Promotion promotion) {
        return PromotionEntity.builder()
                .id(promotion.getId().getValue())
                .code(promotion.getId().getValue())
                .name(promotion.getName())
                .build();
    }

    private Promotion toDomain(PromotionEntity entity) {
        return Promotion.builder()
                .id(new PromotionId(entity.getId()))
                .name(entity.getName())
                .rule(orderTotal -> orderTotal > 0)
                .build();
    }
}
