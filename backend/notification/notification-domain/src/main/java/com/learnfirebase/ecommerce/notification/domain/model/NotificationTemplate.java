package com.learnfirebase.ecommerce.notification.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {
    private String code;
    private NotificationChannel channel;
    private String subject;
    private String body;
}
