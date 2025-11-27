package com.learnfirebase.ecommerce.promotion.application.port.in;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.promotion.application.command.ApplyPromotionCommand;
import com.learnfirebase.ecommerce.promotion.application.dto.PromotionResultDto;

public interface ApplyPromotionUseCase extends UseCase {
    PromotionResultDto execute(ApplyPromotionCommand command);
}
