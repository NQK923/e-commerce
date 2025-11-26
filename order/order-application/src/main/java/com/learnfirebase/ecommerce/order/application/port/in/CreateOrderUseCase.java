package com.learnfirebase.ecommerce.order.application.port.in;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.order.application.command.CreateOrderCommand;
import com.learnfirebase.ecommerce.order.application.dto.OrderDto;

public interface CreateOrderUseCase extends UseCase<CreateOrderCommand, OrderDto> {
}
