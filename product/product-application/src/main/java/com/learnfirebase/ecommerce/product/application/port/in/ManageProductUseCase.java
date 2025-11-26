package com.learnfirebase.ecommerce.product.application.port.in;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.product.application.command.UpsertProductCommand;
import com.learnfirebase.ecommerce.product.application.dto.ProductDto;

public interface ManageProductUseCase extends UseCase<UpsertProductCommand, ProductDto> {
}
