package com.learnfirebase.ecommerce.notification.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.learnfirebase.ecommerce.notification.application.port.out.EmailGateway;
import com.learnfirebase.ecommerce.notification.application.port.out.NotificationTemplateRepository;
import com.learnfirebase.ecommerce.notification.application.port.out.PushGateway;
import com.learnfirebase.ecommerce.notification.application.port.out.SmsGateway;
import com.learnfirebase.ecommerce.notification.application.service.NotificationApplicationService;

@Configuration
public class NotificationModuleConfig {
    @Bean
    public NotificationApplicationService notificationApplicationService(
        NotificationTemplateRepository templateRepository,
        EmailGateway emailGateway,
        PushGateway pushGateway,
        SmsGateway smsGateway
    ) {
        return new NotificationApplicationService(templateRepository, emailGateway, pushGateway, smsGateway);
    }
}
