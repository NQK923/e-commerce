package com.learnfirebase.ecommerce.promotion.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.learnfirebase.ecommerce.promotion.application.port.out.PromotionRepository;
import com.learnfirebase.ecommerce.promotion.application.port.out.PromotionUsageRepository;
import com.learnfirebase.ecommerce.promotion.application.service.PromotionApplicationService;
import com.learnfirebase.ecommerce.promotion.infrastructure.persistence.PromotionEntity;
import com.learnfirebase.ecommerce.promotion.infrastructure.persistence.PromotionJpaRepository;

@Configuration
@EnableJpaRepositories(basePackageClasses = PromotionJpaRepository.class)
@EntityScan(basePackageClasses = PromotionEntity.class)
public class PromotionModuleConfig {
    @Bean
    public PromotionApplicationService promotionApplicationService(PromotionRepository promotionRepository, PromotionUsageRepository promotionUsageRepository) {
        return new PromotionApplicationService(promotionRepository, promotionUsageRepository);
    }
}
