package com.learnfirebase.ecommerce.order.application.port.in;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;

import com.learnfirebase.ecommerce.order.application.dto.OutboxEventDto;

public interface QueryFailedOutboxEventsUseCase {
    PageResponse<OutboxEventDto> queryFailedEvents(PageRequest pageRequest);
}
