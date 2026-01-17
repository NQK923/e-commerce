package com.learnfirebase.ecommerce.notification.adapter.websocket;

import com.learnfirebase.ecommerce.notification.application.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket controller for real-time notifications.
 * Reuses existing STOMP infrastructure from chat module.
 */
@Controller
@RequiredArgsConstructor
public class NotificationWebSocketController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     * Subscribe to user's notification stream.
     * Client subscribes to: /user/queue/notifications
     */
    @SubscribeMapping("/notifications")
    public void subscribe(Principal principal) {
        // Called when user subscribes to their notification queue
        // Return value would be sent to subscriber, but we'll push notifications
        // separately
    }

    /**
     * Send notification to specific user via WebSocket.
     * This method can be called from notification service/use case.
     */
    public void sendNotificationToUser(String userId, NotificationDto notification) {
        simpMessagingTemplate.convertAndSendToUser(
                java.util.Objects.requireNonNull(userId, "userId must not be null"),
                "/queue/notifications",
                java.util.Objects.requireNonNull(notification, "notification must not be null"));
    }

    /**
     * Acknowledge notification read from client.
     */
    @MessageMapping("/notification.read")
    public void markRead(@Payload NotificationReadRequest request, Principal principal) {
        // Optionally handle client-side read acknowledgment
        // The actual marking is done via REST API
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NotificationReadRequest {
        private String notificationId;
    }
}
