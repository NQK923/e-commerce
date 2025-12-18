package com.learnfirebase.ecommerce.order.application.port.in;

import com.learnfirebase.ecommerce.order.application.command.RequestReturnCommand;
import com.learnfirebase.ecommerce.order.application.dto.OrderDto;

public interface RequestReturnUseCase {
    OrderDto requestReturn(RequestReturnCommand command);
}
