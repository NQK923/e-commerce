package com.learnfirebase.ecommerce.chat.infrastructure.presence;

import java.time.Instant;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketPresenceEventListener {

    private static final String ONLINE_USERS_KEY = "chat:online-users";

    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public WebSocketPresenceEventListener(StringRedisTemplate stringRedisTemplate,
                                          SimpMessagingTemplate simpMessagingTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        var accessor = StompHeaderAccessor.wrap(event.getMessage());
        var user = accessor.getUser();
        if (user != null && user.getName() != null) {
            String userId = user.getName();
            stringRedisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId);
            publishPresence(userId, true);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        var user = event.getUser();
        if (user != null && user.getName() != null) {
            String userId = user.getName();
            stringRedisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId);
            publishPresence(userId, false);
        }
    }

    private void publishPresence(String userId, boolean online) {
        simpMessagingTemplate.convertAndSend("/topic/chat/presence",
                new PresenceEvent(userId, online, Instant.now().toString()));
    }

    public record PresenceEvent(String userId, boolean online, String lastActiveAt) { }
}
