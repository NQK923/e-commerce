package com.learnfirebase.ecommerce.notification.application.port.in;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.notification.application.command.RecordNotificationCommand;
import com.learnfirebase.ecommerce.notification.application.dto.NotificationDto;

public interface RecordNotificationUseCase extends UseCase {
    NotificationDto record(RecordNotificationCommand command);
}
