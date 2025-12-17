package com.learnfirebase.ecommerce.notification.application.dto;

import java.time.Instant;

import com.learnfirebase.ecommerce.notification.domain.model.NotificationChannel;
import com.learnfirebase.ecommerce.notification.domain.model.NotificationStatus;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NotificationDto {
    String id;
    String userId;
    String title;
    String body;
    NotificationChannel channel;
    NotificationStatus status;
    Instant createdAt;
    Instant readAt;
}
