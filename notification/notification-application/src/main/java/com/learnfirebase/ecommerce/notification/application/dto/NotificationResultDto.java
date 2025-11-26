package com.learnfirebase.ecommerce.notification.application.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NotificationResultDto {
    String status;
    String messageId;
}
