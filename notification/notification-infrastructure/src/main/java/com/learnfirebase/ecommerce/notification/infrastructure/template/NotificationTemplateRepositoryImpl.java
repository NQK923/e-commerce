package com.learnfirebase.ecommerce.notification.infrastructure.template;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.learnfirebase.ecommerce.notification.application.port.out.NotificationTemplateRepository;
import com.learnfirebase.ecommerce.notification.domain.model.NotificationChannel;
import com.learnfirebase.ecommerce.notification.domain.model.NotificationTemplate;

@Repository
public class NotificationTemplateRepositoryImpl implements NotificationTemplateRepository {
    @Override
    public Optional<NotificationTemplate> findByCode(String code) {
        return Optional.of(NotificationTemplate.builder()
            .code(code)
            .channel(NotificationChannel.EMAIL)
            .subject("Default")
            .body("Hello")
            .build());
    }
}
