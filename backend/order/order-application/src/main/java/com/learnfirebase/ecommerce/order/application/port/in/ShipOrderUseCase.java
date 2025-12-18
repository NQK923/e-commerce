package com.learnfirebase.ecommerce.order.application.port.in;

import com.learnfirebase.ecommerce.order.application.command.ShipOrderCommand;
import com.learnfirebase.ecommerce.order.application.dto.OrderDto;

public interface ShipOrderUseCase {
    OrderDto ship(ShipOrderCommand command);
}
