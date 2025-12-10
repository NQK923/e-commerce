package com.learnfirebase.ecommerce.chat.infrastructure.presence;

import com.learnfirebase.ecommerce.chat.application.port.out.PresencePort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisPresenceAdapter implements PresencePort {

    private static final String ONLINE_USERS_KEY = "chat:online-users";
    private final StringRedisTemplate stringRedisTemplate;

    public RedisPresenceAdapter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean isOnline(String userId) {
        Boolean member = stringRedisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, userId);
        return Boolean.TRUE.equals(member);
    }
}
