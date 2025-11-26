package com.learnfirebase.ecommerce.cart.application.port.in;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.cart.application.command.AddItemCommand;
import com.learnfirebase.ecommerce.cart.application.dto.CartDto;

public interface ManageCartUseCase extends UseCase<AddItemCommand, CartDto> {
}
