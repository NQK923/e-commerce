package com.learnfirebase.ecommerce.chat.infrastructure.security;

import javax.crypto.SecretKey;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final SecretKey key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        this.key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String validateAndGetUserId(String token) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer("ecommerce-auth")
                    .requireAudience("ecommerce-app")
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            if (!"access".equals(claims.get("type"))) {
                throw new IllegalArgumentException("Token is not an access token");
            }
            
            return claims.getSubject();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JWT token", ex);
        }
    }
}
