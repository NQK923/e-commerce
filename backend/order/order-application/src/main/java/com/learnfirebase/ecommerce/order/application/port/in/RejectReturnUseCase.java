package com.learnfirebase.ecommerce.order.application.port.in;

import com.learnfirebase.ecommerce.order.application.command.RejectReturnCommand;
import com.learnfirebase.ecommerce.order.application.dto.OrderDto;

public interface RejectReturnUseCase {
    OrderDto rejectReturn(RejectReturnCommand command);
}
