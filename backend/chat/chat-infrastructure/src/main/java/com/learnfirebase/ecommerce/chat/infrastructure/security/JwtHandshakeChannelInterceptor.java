package com.learnfirebase.ecommerce.chat.infrastructure.security;

import java.util.List;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtHandshakeChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtHandshakeChannelInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractBearer(accessor.getNativeHeader("Authorization"));
            String userId = jwtTokenProvider.validateAndGetUserId(token);
            Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(userId, token, List.of());
            accessor.setUser(authentication);
        }
        return message;
    }

    private String extractBearer(List<String> authHeaders) {
        String token = (authHeaders != null && !authHeaders.isEmpty()) ? authHeaders.get(0) : null;
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }
        return token.substring("Bearer ".length());
    }
}
