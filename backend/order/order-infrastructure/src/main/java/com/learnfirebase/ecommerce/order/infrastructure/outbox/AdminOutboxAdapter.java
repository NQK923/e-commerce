package com.learnfirebase.ecommerce.order.infrastructure.outbox;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.order.application.dto.OutboxEventDto;
import com.learnfirebase.ecommerce.order.application.port.out.AdminOutboxPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminOutboxAdapter implements AdminOutboxPort {

    private final OutboxRepository outboxRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OutboxEventDto> findByStatuses(List<String> statuses, com.learnfirebase.ecommerce.common.application.pagination.PageRequest pageRequest) {
        List<OutboxStatus> enumStatuses = statuses.stream()
                .map(OutboxStatus::valueOf)
                .toList();
                
        Pageable pageable = PageRequest.of(pageRequest.getPage(), pageRequest.getSize());
        Page<OutboxEntity> page = outboxRepository.findByStatusIn(enumStatuses, pageable);
        
        List<OutboxEventDto> dtos = page.getContent().stream().map(this::toDto).toList();
        
        return PageResponse.<OutboxEventDto>builder()
                .content(dtos)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public void retryEvent(String eventId) {
        outboxRepository.findById(eventId).ifPresent(entity -> {
            entity.setStatus(OutboxStatus.PENDING);
            entity.setNextRetryAt(null);
            entity.setDeadLetterAt(null);
            entity.setLastError(null);
            entity.setAttemptCount(0);
            outboxRepository.save(entity);
        });
    }

    private OutboxEventDto toDto(OutboxEntity entity) {
        return OutboxEventDto.builder()
                .id(entity.getId())
                .aggregateId(entity.getAggregateId())
                .type(entity.getType())
                .payload(entity.getPayload())
                .status(entity.getStatus().name())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .attemptCount(entity.getAttemptCount())
                .lastError(entity.getLastError())
                .nextRetryAt(entity.getNextRetryAt())
                .deadLetterAt(entity.getDeadLetterAt())
                .build();
    }
}
