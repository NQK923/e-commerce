package com.learnfirebase.ecommerce.identity.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

class JwtTokenProviderImplTest {

    private JwtTokenProviderImpl provider;
    private final String testSecret = "myTestSecretKeyForJwtTokenProviderWithMoreThan32Bytes";

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProviderImpl(testSecret);
    }

    @Test
    void shouldGenerateValidToken() {
        String userId = "user123";
        String email = "test@example.com";
        String accessToken = provider.generateAccessToken(userId, email);

        assertThat(accessToken).isNotEmpty();
        
        String parsedUserId = provider.validateAndGetUserId(accessToken);
        assertThat(parsedUserId).isEqualTo(userId);
    }

    @Test
    void shouldRejectInvalidToken() {
        assertThatThrownBy(() -> provider.validateAndGetUserId("invalidTokenString"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectTamperedToken() {
        String accessToken = provider.generateAccessToken("user123", "test@example.com");
        String tamperedToken = accessToken + "tampered";
        
        assertThatThrownBy(() -> provider.validateAndGetUserId(tamperedToken))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectForWrongSecret() {
        String accessToken = provider.generateAccessToken("user123", "test@example.com");
        JwtTokenProviderImpl wrongProvider = new JwtTokenProviderImpl("anotherSecretKeyThatIsAlsoMoreThan32BytesLong");
        
        assertThatThrownBy(() -> wrongProvider.validateAndGetUserId(accessToken))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectRefreshToken() {
        String refreshToken = provider.generateRefreshToken("user123", "test@example.com", "device1");
        assertThatThrownBy(() -> provider.validateAndGetUserId(refreshToken))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid JWT token");
    }

    @Test
    void shouldRejectExpiredToken() {
        SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(testSecret.getBytes());
        Date past = new Date(System.currentTimeMillis() - 10000);
        String expiredToken = Jwts.builder()
                .subject("user123")
                .claim("type", "access")
                .issuer("ecommerce-auth")
                .audience().add("ecommerce-app").and()
                .expiration(past)
                .signWith(key)
                .compact();
                
        assertThatThrownBy(() -> provider.validateAndGetUserId(expiredToken))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectWrongIssuer() {
        SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(testSecret.getBytes());
        Date future = new Date(System.currentTimeMillis() + 10000);
        String wrongIssuerToken = Jwts.builder()
                .subject("user123")
                .claim("type", "access")
                .issuer("wrong-issuer")
                .audience().add("ecommerce-app").and()
                .expiration(future)
                .signWith(key)
                .compact();
                
        assertThatThrownBy(() -> provider.validateAndGetUserId(wrongIssuerToken))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectWrongAudience() {
        SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(testSecret.getBytes());
        Date future = new Date(System.currentTimeMillis() + 10000);
        String wrongAudToken = Jwts.builder()
                .subject("user123")
                .claim("type", "access")
                .issuer("ecommerce-auth")
                .audience().add("wrong-app").and()
                .expiration(future)
                .signWith(key)
                .compact();
                
        assertThatThrownBy(() -> provider.validateAndGetUserId(wrongAudToken))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
