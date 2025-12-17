package com.learnfirebase.ecommerce.notification.application.port.out;

import java.util.List;
import java.util.Optional;

import com.learnfirebase.ecommerce.notification.application.dto.NotificationDto;
import com.learnfirebase.ecommerce.notification.domain.model.NotificationStatus;

public interface NotificationRepository {
    NotificationDto save(NotificationDto notification);
    List<NotificationDto> findByUser(String userId, int limit);
    Optional<NotificationDto> findById(String id);
    long countUnread(String userId);
    void updateStatus(String id, NotificationStatus status);
}
