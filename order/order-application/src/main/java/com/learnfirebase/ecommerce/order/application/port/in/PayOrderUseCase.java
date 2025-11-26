package com.learnfirebase.ecommerce.order.application.port.in;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.order.application.command.PayOrderCommand;
import com.learnfirebase.ecommerce.order.application.dto.OrderDto;

public interface PayOrderUseCase extends UseCase {
    OrderDto execute(PayOrderCommand command);
}
