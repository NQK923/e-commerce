package com.learnfirebase.ecommerce.notification.application.service;

import java.util.UUID;

import com.learnfirebase.ecommerce.common.domain.AccessDeniedDomainException;
import com.learnfirebase.ecommerce.common.domain.DomainException;
import com.learnfirebase.ecommerce.notification.application.command.SendNotificationCommand;
import com.learnfirebase.ecommerce.notification.application.command.RecordNotificationCommand;
import com.learnfirebase.ecommerce.notification.application.command.MarkNotificationReadCommand;
import com.learnfirebase.ecommerce.notification.application.dto.NotificationResultDto;
import com.learnfirebase.ecommerce.notification.application.dto.NotificationDto;
import com.learnfirebase.ecommerce.notification.application.port.in.SendNotificationUseCase;
import com.learnfirebase.ecommerce.notification.application.port.in.RecordNotificationUseCase;
import com.learnfirebase.ecommerce.notification.application.port.in.ListNotificationsUseCase;
import com.learnfirebase.ecommerce.notification.application.port.in.MarkNotificationUseCase;
import com.learnfirebase.ecommerce.notification.application.port.out.NotificationRepository;
import com.learnfirebase.ecommerce.notification.application.port.out.EmailGateway;
import com.learnfirebase.ecommerce.notification.application.port.out.NotificationTemplateRepository;
import com.learnfirebase.ecommerce.notification.application.port.out.PushGateway;
import com.learnfirebase.ecommerce.notification.application.port.out.RealtimeNotificationGateway;
import com.learnfirebase.ecommerce.notification.domain.model.NotificationChannel;
import com.learnfirebase.ecommerce.notification.domain.model.NotificationTemplate;
import com.learnfirebase.ecommerce.notification.domain.model.NotificationStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationApplicationService implements SendNotificationUseCase, RecordNotificationUseCase, ListNotificationsUseCase, MarkNotificationUseCase {
    private final NotificationTemplateRepository templateRepository;
    private final EmailGateway emailGateway;
    private final PushGateway pushGateway;
    private final NotificationRepository notificationRepository;
    private final RealtimeNotificationGateway realtimeNotificationGateway;

    @Override
    public NotificationResultDto execute(SendNotificationCommand command) {
        NotificationTemplate template = templateRepository.findByCode(command.getTemplateCode())
            .orElse(NotificationTemplate.builder().code(command.getTemplateCode()).channel(command.getChannel()).subject("Notification").body(command.getPayload()).build());

        String messageId;
        NotificationChannel channel = command.getChannel() != null ? command.getChannel() : template.getChannel();
        switch (channel) {
            case EMAIL -> messageId = emailGateway.send(command.getRecipient(), template.getSubject(), template.getBody());
            case PUSH -> messageId = pushGateway.send(command.getRecipient(), template.getBody());
            default -> messageId = UUID.randomUUID().toString();
        }
        return NotificationResultDto.builder()
            .status("SENT")
            .messageId(messageId)
            .build();
    }

    public NotificationDto record(RecordNotificationCommand command) {
        NotificationDto dto = NotificationDto.builder()
            .id(UUID.randomUUID().toString())
            .userId(command.getUserId())
            .title(command.getTitle())
            .body(command.getBody())
            .channel(command.getChannel() != null ? command.getChannel() : NotificationChannel.PUSH)
            .status(NotificationStatus.UNREAD)
            .createdAt(java.time.Instant.now())
            .build();
        NotificationDto saved = notificationRepository.save(dto);
        realtimeNotificationGateway.deliver(saved);
        return saved;
    }

    public java.util.List<NotificationDto> list(String userId, int limit) {
        return notificationRepository.findByUser(userId, limit);
    }

    public void markRead(MarkNotificationReadCommand command) {
        NotificationDto notification = notificationRepository.findById(command.getNotificationId())
            .orElseThrow(() -> new DomainException("Notification not found"));
        if (command.getUserId() == null || !command.getUserId().equals(notification.getUserId())) {
            throw new AccessDeniedDomainException("Only notification owner can mark it as read");
        }
        notificationRepository.updateStatus(command.getNotificationId(), NotificationStatus.READ);
    }

    public long unreadCount(String userId) {
        return notificationRepository.countUnread(userId);
    }
}
