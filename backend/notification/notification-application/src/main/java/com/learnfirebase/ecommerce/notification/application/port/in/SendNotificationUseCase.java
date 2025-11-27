package com.learnfirebase.ecommerce.notification.application.port.in;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.notification.application.command.SendNotificationCommand;
import com.learnfirebase.ecommerce.notification.application.dto.NotificationResultDto;

public interface SendNotificationUseCase extends UseCase {
    NotificationResultDto execute(SendNotificationCommand command);
}
