package com.learnfirebase.ecommerce.notification.application.port.out;

import com.learnfirebase.ecommerce.notification.application.dto.NotificationDto;

public interface RealtimeNotificationGateway {
    void deliver(NotificationDto notification);
}
