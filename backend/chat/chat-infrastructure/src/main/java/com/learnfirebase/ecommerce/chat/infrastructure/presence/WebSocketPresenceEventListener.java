package com.learnfirebase.ecommerce.chat.infrastructure.presence;

import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketPresenceEventListener {

    private static final String ONLINE_USERS_KEY = "chat:online-users";

    private final StringRedisTemplate stringRedisTemplate;

    public WebSocketPresenceEventListener(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        var accessor = StompHeaderAccessor.wrap(event.getMessage());
        var user = accessor.getUser();
        if (user != null && user.getName() != null) {
            stringRedisTemplate.opsForSet().add(ONLINE_USERS_KEY, user.getName());
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        var user = event.getUser();
        if (user != null && user.getName() != null) {
            stringRedisTemplate.opsForSet().remove(ONLINE_USERS_KEY, user.getName());
        }
    }
}
