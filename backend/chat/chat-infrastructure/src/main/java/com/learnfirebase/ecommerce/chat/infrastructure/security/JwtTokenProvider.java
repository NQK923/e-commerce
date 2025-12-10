package com.learnfirebase.ecommerce.chat.infrastructure.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtDecoder jwtDecoder;

    public JwtTokenProvider(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    public String validateAndGetUserId(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getSubject();
        } catch (JwtException ex) {
            throw new IllegalArgumentException("Invalid JWT token", ex);
        }
    }
}
