package com.learnfirebase.ecommerce.identity.infrastructure.security;

import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.learnfirebase.ecommerce.identity.application.port.out.TokenProvider;

@Component
public class JwtTokenProviderImpl implements TokenProvider {

    private final SecretKey key;
    private final long accessTokenValidityMs = 3600000; // 1 hour
    private final long refreshTokenValidityMs = 86400000L * 30; // 30 days

    public JwtTokenProviderImpl(@Value("${jwt.secret}") String secret) {
        this.key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public String generateAccessToken(String userId, String email) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("type", "access")
                .issuer("ecommerce-auth")
                .audience().add("ecommerce-app").and()
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenValidityMs))
                .signWith(key)
                .compact();
    }

    @Override
    public String generateRefreshToken(String userId, String email, String deviceId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("deviceId", deviceId)
                .claim("type", "refresh")
                .issuer("ecommerce-auth")
                .audience().add("ecommerce-app").and()
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenValidityMs))
                .signWith(key)
                .compact();
    }

    @Override
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
