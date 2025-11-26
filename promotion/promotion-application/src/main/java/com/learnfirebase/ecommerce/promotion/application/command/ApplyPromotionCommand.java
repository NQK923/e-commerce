package com.learnfirebase.ecommerce.promotion.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApplyPromotionCommand {
    String promotionCode;
    double orderTotal;
    String userId;
}
