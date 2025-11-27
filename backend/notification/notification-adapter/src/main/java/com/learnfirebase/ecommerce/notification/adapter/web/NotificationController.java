package com.learnfirebase.ecommerce.notification.adapter.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.notification.application.command.SendNotificationCommand;
import com.learnfirebase.ecommerce.notification.application.dto.NotificationResultDto;
import com.learnfirebase.ecommerce.notification.application.port.in.SendNotificationUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final SendNotificationUseCase sendNotificationUseCase;

    @PostMapping
    public ResponseEntity<NotificationResultDto> send(@RequestBody SendNotificationCommand command) {
        return ResponseEntity.ok(sendNotificationUseCase.execute(command));
    }
}
