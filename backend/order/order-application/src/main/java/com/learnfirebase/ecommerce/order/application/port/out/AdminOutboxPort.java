package com.learnfirebase.ecommerce.order.application.port.out;

import java.util.List;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;

import com.learnfirebase.ecommerce.order.application.dto.OutboxEventDto;

public interface AdminOutboxPort {
    PageResponse<OutboxEventDto> findByStatuses(List<String> statuses, PageRequest pageRequest);
    void retryEvent(String eventId);
}
