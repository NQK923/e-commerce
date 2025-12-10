package com.learnfirebase.ecommerce.chat.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    /**
     * Identity module issues simple Base64 tokens: userId:email:access.
     * Decode and extract userId; reject malformed tokens.
     */
    public String validateAndGetUserId(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length < 1 || parts[0].isBlank()) {
                throw new IllegalArgumentException("Token missing user id");
            }
            return parts[0];
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid token", ex);
        }
    }
}
