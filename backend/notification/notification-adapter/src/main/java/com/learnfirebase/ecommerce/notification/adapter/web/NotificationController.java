package com.learnfirebase.ecommerce.notification.adapter.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.notification.application.command.SendNotificationCommand;
import com.learnfirebase.ecommerce.notification.application.dto.NotificationResultDto;
import com.learnfirebase.ecommerce.notification.application.port.in.SendNotificationUseCase;
import com.learnfirebase.ecommerce.notification.application.port.in.RecordNotificationUseCase;
import com.learnfirebase.ecommerce.notification.application.port.in.ListNotificationsUseCase;
import com.learnfirebase.ecommerce.notification.application.port.in.MarkNotificationUseCase;
import com.learnfirebase.ecommerce.notification.application.command.RecordNotificationCommand;
import com.learnfirebase.ecommerce.notification.application.command.MarkNotificationReadCommand;
import com.learnfirebase.ecommerce.notification.application.dto.NotificationDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final SendNotificationUseCase sendNotificationUseCase;
    private final RecordNotificationUseCase recordNotificationUseCase;
    private final ListNotificationsUseCase listNotificationsUseCase;
    private final MarkNotificationUseCase markNotificationUseCase;

    @PostMapping
    public ResponseEntity<NotificationResultDto> send(@RequestBody SendNotificationCommand command) {
        return ResponseEntity.ok(sendNotificationUseCase.execute(command));
    }

    @PostMapping("/record")
    public ResponseEntity<NotificationDto> record(@RequestBody RecordNotificationCommand command) {
        return ResponseEntity.ok(recordNotificationUseCase.record(command));
    }

    @GetMapping
    public ResponseEntity<java.util.List<NotificationDto>> list(
        @RequestParam("userId") String userId,
        @RequestParam(name = "limit", defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(listNotificationsUseCase.list(userId, limit));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount(@RequestParam("userId") String userId) {
        return ResponseEntity.ok(listNotificationsUseCase.unreadCount(userId));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable("id") String id, @RequestParam("userId") String userId) {
        markNotificationUseCase.markRead(MarkNotificationReadCommand.builder().notificationId(id).userId(userId).build());
        return ResponseEntity.ok().build();
    }
}
