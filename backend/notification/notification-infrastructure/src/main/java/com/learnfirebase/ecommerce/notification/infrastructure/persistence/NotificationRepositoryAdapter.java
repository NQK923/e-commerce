package com.learnfirebase.ecommerce.notification.infrastructure.persistence;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.learnfirebase.ecommerce.notification.application.dto.NotificationDto;
import com.learnfirebase.ecommerce.notification.application.port.out.NotificationRepository;
import com.learnfirebase.ecommerce.notification.domain.model.NotificationStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;

    @Override
    @Transactional
    public NotificationDto save(NotificationDto notification) {
        NotificationEntity entity = toEntity(notification);
        return toDto(jpaRepository.save(Objects.requireNonNull(entity)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> findByUser(String userId, int limit) {
        return jpaRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(limit > 0 ? limit : 20)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationDto> findById(String id) {
        return jpaRepository.findById(Objects.requireNonNull(id)).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(String userId) {
        return jpaRepository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD);
    }

    @Override
    @Transactional
    public void updateStatus(String id, NotificationStatus status) {
        jpaRepository.updateStatus(id, status);
    }

    private NotificationDto toDto(NotificationEntity entity) {
        return NotificationDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .body(entity.getBody())
                .channel(entity.getChannel())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .readAt(entity.getReadAt())
                .build();
    }

    private NotificationEntity toEntity(NotificationDto dto) {
        return NotificationEntity.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .title(dto.getTitle())
                .body(dto.getBody())
                .channel(dto.getChannel())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .readAt(dto.getReadAt())
                .build();
    }
}
