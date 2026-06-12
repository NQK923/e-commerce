package com.learnfirebase.ecommerce.notification.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.learnfirebase.ecommerce.common.domain.AccessDeniedDomainException;
import com.learnfirebase.ecommerce.notification.application.command.MarkNotificationReadCommand;
import com.learnfirebase.ecommerce.notification.application.dto.NotificationDto;
import com.learnfirebase.ecommerce.notification.application.port.out.EmailGateway;
import com.learnfirebase.ecommerce.notification.application.port.out.NotificationRepository;
import com.learnfirebase.ecommerce.notification.application.port.out.NotificationTemplateRepository;
import com.learnfirebase.ecommerce.notification.application.port.out.PushGateway;
import com.learnfirebase.ecommerce.notification.domain.model.NotificationChannel;
import com.learnfirebase.ecommerce.notification.domain.model.NotificationStatus;

@ExtendWith(MockitoExtension.class)
class NotificationApplicationServiceTest {
    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private EmailGateway emailGateway;

    @Mock
    private PushGateway pushGateway;

    @Mock
    private NotificationRepository notificationRepository;

    @Test
    void markReadRejectsNonOwner() {
        NotificationApplicationService service = service();
        when(notificationRepository.findById("note-1")).thenReturn(Optional.of(notification("owner-1")));

        assertThrows(AccessDeniedDomainException.class, () ->
            service.markRead(MarkNotificationReadCommand.builder()
                .notificationId("note-1")
                .userId("other-user")
                .build()));

        verify(notificationRepository, never()).updateStatus("note-1", NotificationStatus.READ);
    }

    @Test
    void markReadUpdatesOwnerNotification() {
        NotificationApplicationService service = service();
        when(notificationRepository.findById("note-1")).thenReturn(Optional.of(notification("owner-1")));

        service.markRead(MarkNotificationReadCommand.builder()
            .notificationId("note-1")
            .userId("owner-1")
            .build());

        verify(notificationRepository).updateStatus("note-1", NotificationStatus.READ);
    }

    private NotificationApplicationService service() {
        return new NotificationApplicationService(templateRepository, emailGateway, pushGateway, notificationRepository);
    }

    private NotificationDto notification(String userId) {
        return NotificationDto.builder()
            .id("note-1")
            .userId(userId)
            .title("Title")
            .body("Body")
            .channel(NotificationChannel.PUSH)
            .status(NotificationStatus.UNREAD)
            .createdAt(Instant.now())
            .build();
    }
}
