package com.learnfirebase.ecommerce.notification.application.port.out;

import java.util.Optional;

import com.learnfirebase.ecommerce.notification.domain.model.NotificationTemplate;

public interface NotificationTemplateRepository {
    Optional<NotificationTemplate> findByCode(String code);
}
