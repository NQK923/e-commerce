package com.learnfirebase.ecommerce.order.application.port.in;

import com.learnfirebase.ecommerce.order.application.command.ApproveReturnCommand;
import com.learnfirebase.ecommerce.order.application.dto.OrderDto;

public interface ApproveReturnUseCase {
    OrderDto approveReturn(ApproveReturnCommand command);
}
