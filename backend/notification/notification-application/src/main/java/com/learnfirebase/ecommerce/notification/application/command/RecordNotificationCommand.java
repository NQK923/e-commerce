package com.learnfirebase.ecommerce.notification.application.command;

import com.learnfirebase.ecommerce.notification.domain.model.NotificationChannel;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RecordNotificationCommand {
    String userId;
    String title;
    String body;
    NotificationChannel channel;
}
