package com.learnfirebase.ecommerce.notification.application.service;

import java.util.UUID;

import com.learnfirebase.ecommerce.notification.application.command.SendNotificationCommand;
import com.learnfirebase.ecommerce.notification.application.dto.NotificationResultDto;
import com.learnfirebase.ecommerce.notification.application.port.in.SendNotificationUseCase;
import com.learnfirebase.ecommerce.notification.application.port.out.EmailGateway;
import com.learnfirebase.ecommerce.notification.application.port.out.NotificationTemplateRepository;
import com.learnfirebase.ecommerce.notification.application.port.out.PushGateway;
import com.learnfirebase.ecommerce.notification.application.port.out.SmsGateway;
import com.learnfirebase.ecommerce.notification.domain.model.NotificationChannel;
import com.learnfirebase.ecommerce.notification.domain.model.NotificationTemplate;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationApplicationService implements SendNotificationUseCase {
    private final NotificationTemplateRepository templateRepository;
    private final EmailGateway emailGateway;
    private final PushGateway pushGateway;
    private final SmsGateway smsGateway;

    @Override
    public NotificationResultDto execute(SendNotificationCommand command) {
        NotificationTemplate template = templateRepository.findByCode(command.getTemplateCode())
            .orElse(NotificationTemplate.builder().code(command.getTemplateCode()).channel(command.getChannel()).subject("Notification").body(command.getPayload()).build());

        String messageId;
        NotificationChannel channel = command.getChannel() != null ? command.getChannel() : template.getChannel();
        switch (channel) {
            case EMAIL -> messageId = emailGateway.send(command.getRecipient(), template.getSubject(), template.getBody());
            case PUSH -> messageId = pushGateway.send(command.getRecipient(), template.getBody());
            case SMS -> messageId = smsGateway.send(command.getRecipient(), template.getBody());
            default -> messageId = UUID.randomUUID().toString();
        }
        return NotificationResultDto.builder()
            .status("SENT")
            .messageId(messageId)
            .build();
    }
}
