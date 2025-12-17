package com.learnfirebase.ecommerce.order.application.port.in;

import com.learnfirebase.ecommerce.order.application.command.HandlePaymentCallbackCommand;
import com.learnfirebase.ecommerce.order.application.dto.OrderDto;

public interface HandlePaymentCallbackUseCase extends UseCase {
    OrderDto handleCallback(HandlePaymentCallbackCommand command);
}
