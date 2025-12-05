package com.learnfirebase.ecommerce.order.application.port.in;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.order.application.dto.OrderDto;

public interface ListOrdersUseCase {
    PageResponse<OrderDto> listOrders(PageRequest pageRequest);
}
