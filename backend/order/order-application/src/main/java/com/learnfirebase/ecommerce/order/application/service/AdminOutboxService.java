package com.learnfirebase.ecommerce.order.application.service;

import java.util.List;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;

import com.learnfirebase.ecommerce.order.application.dto.OutboxEventDto;
import com.learnfirebase.ecommerce.order.application.port.in.QueryFailedOutboxEventsUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.RetryOutboxEventUseCase;
import com.learnfirebase.ecommerce.order.application.port.out.AdminOutboxPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AdminOutboxService implements QueryFailedOutboxEventsUseCase, RetryOutboxEventUseCase {

    private final AdminOutboxPort adminOutboxPort;

    @Override
    public PageResponse<OutboxEventDto> queryFailedEvents(PageRequest pageRequest) {
        return adminOutboxPort.findByStatuses(List.of("FAILED", "DEAD_LETTER"), pageRequest);
    }

    @Override
    public void retryEvent(String eventId) {
        adminOutboxPort.retryEvent(eventId);
    }
}
