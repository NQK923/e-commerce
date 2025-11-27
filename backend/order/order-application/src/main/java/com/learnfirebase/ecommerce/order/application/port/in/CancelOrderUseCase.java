package com.learnfirebase.ecommerce.order.application.port.in;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.order.application.command.CancelOrderCommand;
import com.learnfirebase.ecommerce.order.application.dto.OrderDto;

public interface CancelOrderUseCase extends UseCase {
    OrderDto execute(CancelOrderCommand command);
}
