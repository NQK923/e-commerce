package com.learnfirebase.ecommerce.promotion.application.service;

import java.util.UUID;

import com.learnfirebase.ecommerce.promotion.application.command.ApplyPromotionCommand;
import com.learnfirebase.ecommerce.promotion.application.dto.PromotionResultDto;
import com.learnfirebase.ecommerce.promotion.application.port.in.ApplyPromotionUseCase;
import com.learnfirebase.ecommerce.promotion.application.port.out.PromotionRepository;
import com.learnfirebase.ecommerce.promotion.application.port.out.PromotionUsageRepository;
import com.learnfirebase.ecommerce.promotion.domain.model.Promotion;
import com.learnfirebase.ecommerce.promotion.domain.model.PromotionId;
import com.learnfirebase.ecommerce.promotion.domain.model.PromotionRule;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PromotionApplicationService implements ApplyPromotionUseCase {
    private final PromotionRepository promotionRepository;
    private final PromotionUsageRepository promotionUsageRepository;

    @Override
    public PromotionResultDto execute(ApplyPromotionCommand command) {
        Promotion promotion = promotionRepository.findByCode(command.getPromotionCode())
            .orElseGet(() -> createDefaultPromotion(command.getPromotionCode()));
        double discount = promotion.getRule().applies(command.getOrderTotal()) ? command.getOrderTotal() * 0.1 : 0.0;
        double discountedTotal = command.getOrderTotal() - discount;
        promotionUsageRepository.recordUsage(command.getPromotionCode(), command.getUserId());
        return PromotionResultDto.builder()
            .promotionCode(command.getPromotionCode())
            .discountApplied(discount)
            .discountedTotal(discountedTotal)
            .build();
    }

    private Promotion createDefaultPromotion(String code) {
        Promotion promotion = Promotion.builder()
            .id(new PromotionId(UUID.randomUUID().toString()))
            .name("DEFAULT")
            .rule(orderTotal -> orderTotal > 0)
            .build();
        return promotionRepository.save(promotion);
    }
}
