package com.learnfirebase.ecommerce.promotion.application.port.out;

import java.util.Optional;

import com.learnfirebase.ecommerce.promotion.domain.model.Promotion;
import com.learnfirebase.ecommerce.promotion.domain.model.PromotionId;

public interface PromotionRepository {
    Optional<Promotion> findByCode(String code);

    Promotion save(Promotion promotion);

    Optional<Promotion> findById(PromotionId id);
}
