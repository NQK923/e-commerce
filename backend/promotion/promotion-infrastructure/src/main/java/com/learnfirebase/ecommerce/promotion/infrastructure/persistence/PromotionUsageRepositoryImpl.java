package com.learnfirebase.ecommerce.promotion.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.promotion.application.port.out.PromotionUsageRepository;

@Component
public class PromotionUsageRepositoryImpl implements PromotionUsageRepository {
    @Override
    public void recordUsage(String promotionCode, String userId) {
        // noop stub
    }
}
