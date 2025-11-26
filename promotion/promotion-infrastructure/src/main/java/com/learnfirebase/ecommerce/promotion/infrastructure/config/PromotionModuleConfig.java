package com.learnfirebase.ecommerce.promotion.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.learnfirebase.ecommerce.promotion.application.port.out.PromotionRepository;
import com.learnfirebase.ecommerce.promotion.application.port.out.PromotionUsageRepository;
import com.learnfirebase.ecommerce.promotion.application.service.PromotionApplicationService;

@Configuration
public class PromotionModuleConfig {
    @Bean
    public PromotionApplicationService promotionApplicationService(PromotionRepository promotionRepository, PromotionUsageRepository promotionUsageRepository) {
        return new PromotionApplicationService(promotionRepository, promotionUsageRepository);
    }
}
