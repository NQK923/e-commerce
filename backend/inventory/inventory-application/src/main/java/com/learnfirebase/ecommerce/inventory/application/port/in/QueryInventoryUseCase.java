package com.learnfirebase.ecommerce.inventory.application.port.in;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.inventory.application.dto.InventoryDto;

public interface QueryInventoryUseCase extends UseCase {
    InventoryDto getInventoryByProductId(String productId);
}
