package com.learnfirebase.ecommerce.notification.application.command;

import com.learnfirebase.ecommerce.notification.domain.model.NotificationChannel;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SendNotificationCommand {
    String templateCode;
    NotificationChannel channel;
    String recipient;
    String payload;
}
