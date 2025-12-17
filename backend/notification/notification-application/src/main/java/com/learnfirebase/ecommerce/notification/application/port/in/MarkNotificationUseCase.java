package com.learnfirebase.ecommerce.notification.application.port.in;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.notification.application.command.MarkNotificationReadCommand;

public interface MarkNotificationUseCase extends UseCase {
    void markRead(MarkNotificationReadCommand command);
}
