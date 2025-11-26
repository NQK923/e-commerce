package com.learnfirebase.ecommerce.promotion.application.port.out;

public interface PromotionUsageRepository {
    void recordUsage(String promotionCode, String userId);
}
