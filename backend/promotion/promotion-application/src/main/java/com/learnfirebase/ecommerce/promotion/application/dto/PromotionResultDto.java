package com.learnfirebase.ecommerce.promotion.application.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PromotionResultDto {
    String promotionCode;
    double discountedTotal;
    double discountApplied;
}
