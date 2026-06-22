package com.learnfirebase.ecommerce.order.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.order.application.dto.OutboxEventDto;
import com.learnfirebase.ecommerce.order.application.port.in.QueryFailedOutboxEventsUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.RetryOutboxEventUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/outbox")
@RequiredArgsConstructor
public class AdminOutboxController {

    private final QueryFailedOutboxEventsUseCase queryFailedOutboxEventsUseCase;
    private final RetryOutboxEventUseCase retryOutboxEventUseCase;

    @GetMapping("/failed")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PageResponse<OutboxEventDto>> getFailedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.builder().page(page).size(size).build();
        return ResponseEntity.ok(queryFailedOutboxEventsUseCase.queryFailedEvents(pageRequest));
    }

    @PostMapping("/{id}/retry")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> retryEvent(@PathVariable("id") String id) {
        retryOutboxEventUseCase.retryEvent(id);
        return ResponseEntity.ok().build();
    }
}
