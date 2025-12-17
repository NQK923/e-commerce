package com.learnfirebase.ecommerce.notification.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.learnfirebase.ecommerce.notification.application.port.out.EmailGateway;
import com.learnfirebase.ecommerce.notification.application.port.out.NotificationRepository;
import com.learnfirebase.ecommerce.notification.application.port.out.NotificationTemplateRepository;
import com.learnfirebase.ecommerce.notification.application.port.out.PushGateway;
import com.learnfirebase.ecommerce.notification.application.service.NotificationApplicationService;
import com.learnfirebase.ecommerce.notification.infrastructure.persistence.NotificationEntity;
import com.learnfirebase.ecommerce.notification.infrastructure.persistence.NotificationJpaRepository;

@Configuration
@EnableJpaRepositories(basePackageClasses = NotificationJpaRepository.class)
@EntityScan(basePackageClasses = NotificationEntity.class)
public class NotificationModuleConfig {
    @Bean
    public NotificationApplicationService notificationApplicationService(
        NotificationTemplateRepository templateRepository,
        EmailGateway emailGateway,
        PushGateway pushGateway,
        NotificationRepository notificationRepository
    ) {
        return new NotificationApplicationService(templateRepository, emailGateway, pushGateway, notificationRepository);
    }
}
