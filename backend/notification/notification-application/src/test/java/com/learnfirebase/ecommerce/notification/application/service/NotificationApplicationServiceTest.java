package com.learnfirebase.ecommerce.notification.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.learnfirebase.ecommerce.common.domain.AccessDeniedDomainException;
import com.learnfirebase.ecommerce.notification.application.command.RecordNotificationCommand;
import com.learnfirebase.ecommerce.notification.application.command.MarkNotificationReadCommand;
import com.learnfirebase.ecommerce.notification.application.dto.NotificationDto;
import com.learnfirebase.ecommerce.notification.application.port.out.EmailGateway;
import com.learnfirebase.ecommerce.notification.application.port.out.NotificationRepository;
import com.learnfirebase.ecommerce.notification.application.port.out.NotificationTemplateRepository;
import com.learnfirebase.ecommerce.notification.application.port.out.PushGateway;
import com.learnfirebase.ecommerce.notification.application.port.out.RealtimeNotificationGateway;
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

    @Mock
    private RealtimeNotificationGateway realtimeNotificationGateway;

    @Test
    void recordPersistsAndDeliversRealtimeNotification() {
        NotificationApplicationService service = service();
        NotificationDto saved = notification("owner-1");
        when(notificationRepository.save(any(NotificationDto.class))).thenReturn(saved);

        NotificationDto result = service.record(RecordNotificationCommand.builder()
            .userId("owner-1")
            .title("Title")
            .body("Body")
            .channel(NotificationChannel.PUSH)
            .build());

        org.assertj.core.api.Assertions.assertThat(result).isSameAs(saved);
        verify(notificationRepository).save(any(NotificationDto.class));
        verify(realtimeNotificationGateway).deliver(saved);
    }

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
        return new NotificationApplicationService(
            templateRepository,
            emailGateway,
            pushGateway,
            notificationRepository,
            realtimeNotificationGateway
        );
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
