package com.learnfirebase.ecommerce.notification.application.port.in;

import java.util.List;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.notification.application.dto.NotificationDto;

public interface ListNotificationsUseCase extends UseCase {
    List<NotificationDto> list(String userId, int limit);
    long unreadCount(String userId);
}
