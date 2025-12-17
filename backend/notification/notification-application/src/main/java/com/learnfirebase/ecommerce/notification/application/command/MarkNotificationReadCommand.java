package com.learnfirebase.ecommerce.notification.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MarkNotificationReadCommand {
    String notificationId;
    String userId;
}
