package com.learnfirebase.ecommerce.cart.application.port.in;

import com.learnfirebase.ecommerce.cart.application.command.AddItemCommand;
import com.learnfirebase.ecommerce.cart.application.command.ClearCartCommand;
import com.learnfirebase.ecommerce.cart.application.command.MergeCartCommand;
import com.learnfirebase.ecommerce.cart.application.command.RemoveItemCommand;
import com.learnfirebase.ecommerce.cart.application.command.UpdateItemCommand;
import com.learnfirebase.ecommerce.cart.application.dto.CartDto;
import com.learnfirebase.ecommerce.common.application.UseCase;

public interface ManageCartUseCase extends UseCase {
    CartDto addItem(AddItemCommand command);

    CartDto updateItem(UpdateItemCommand command);

    CartDto removeItem(RemoveItemCommand command);

    CartDto merge(MergeCartCommand command);

    CartDto clear(ClearCartCommand command);

    CartDto get(String cartId);
}
