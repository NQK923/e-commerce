package com.learnfirebase.ecommerce.identity.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.identity.application.port.out.TokenProvider;

@Component
public class JwtTokenProviderImpl implements TokenProvider {
    @Override
    public String generateAccessToken(String userId, String email) {
        String random = UUID.randomUUID().toString();
        return Base64.getEncoder().encodeToString((userId + ":" + email + ":access:" + random).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateRefreshToken(String userId, String email, String deviceId) {
        String random = UUID.randomUUID().toString();
        return Base64.getEncoder().encodeToString((userId + ":" + email + ":" + deviceId + ":refresh:" + random).getBytes(StandardCharsets.UTF_8));
    }
}
