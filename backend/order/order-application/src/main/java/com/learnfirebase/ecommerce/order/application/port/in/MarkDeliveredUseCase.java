package com.learnfirebase.ecommerce.order.application.port.in;

import com.learnfirebase.ecommerce.order.application.command.MarkDeliveredCommand;
import com.learnfirebase.ecommerce.order.application.dto.OrderDto;

public interface MarkDeliveredUseCase {
    OrderDto markDelivered(MarkDeliveredCommand command);
}
