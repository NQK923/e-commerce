package com.learnfirebase.ecommerce.promotion.application.port.in;

import com.learnfirebase.ecommerce.promotion.application.command.CreateFlashSaleCommand;
import com.learnfirebase.ecommerce.promotion.domain.model.FlashSaleId;

public interface CreateFlashSaleUseCase {
    FlashSaleId createFlashSale(CreateFlashSaleCommand command);
}
