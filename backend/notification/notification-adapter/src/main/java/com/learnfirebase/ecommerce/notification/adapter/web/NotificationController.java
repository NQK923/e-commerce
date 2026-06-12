package com.learnfirebase.ecommerce.notification.adapter.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final SendNotificationUseCase sendNotificationUseCase;
    private final RecordNotificationUseCase recordNotificationUseCase;
    private final ListNotificationsUseCase listNotificationsUseCase;
    private final MarkNotificationUseCase markNotificationUseCase;

    @PostMapping
    public ResponseEntity<NotificationResultDto> send(@RequestBody SendNotificationCommand command, Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(sendNotificationUseCase.execute(command));
    }

    @PostMapping("/record")
    public ResponseEntity<NotificationDto> record(@RequestBody RecordNotificationCommand command, Authentication authentication) {
        String userId = resolveAllowedUser(command.getUserId(), authentication);
        RecordNotificationCommand secureCommand = RecordNotificationCommand.builder()
                .userId(userId)
                .title(command.getTitle())
                .body(command.getBody())
                .channel(command.getChannel())
                .build();
        return ResponseEntity.ok(recordNotificationUseCase.record(secureCommand));
    }

    @GetMapping
    public ResponseEntity<java.util.List<NotificationDto>> list(
        @RequestParam(name = "userId", required = false) String userId,
        @RequestParam(name = "limit", defaultValue = "20") int limit,
        Authentication authentication
    ) {
        return ResponseEntity.ok(listNotificationsUseCase.list(resolveAllowedUser(userId, authentication), limit));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount(
        @RequestParam(name = "userId", required = false) String userId,
        Authentication authentication
    ) {
        return ResponseEntity.ok(listNotificationsUseCase.unreadCount(resolveAllowedUser(userId, authentication)));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
        @PathVariable("id") String id,
        @RequestParam(name = "userId", required = false) String userId,
        Authentication authentication
    ) {
        markNotificationUseCase.markRead(MarkNotificationReadCommand.builder()
                .notificationId(id)
                .userId(resolveAllowedUser(userId, authentication))
                .build());
        return ResponseEntity.ok().build();
    }

    private String resolveAllowedUser(String requestedUserId, Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        String effectiveUserId = requestedUserId != null && !requestedUserId.isBlank()
                ? requestedUserId
                : authentication.getName();
        if (!effectiveUserId.equals(authentication.getName()) && !hasRole(authentication, "ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to access another user's notifications");
        }
        return effectiveUserId;
    }

    private void requireAdmin(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        if (!hasRole(authentication, "ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role is required");
        }
    }

    private boolean hasRole(Authentication authentication, String role) {
        String authority = "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }
}
