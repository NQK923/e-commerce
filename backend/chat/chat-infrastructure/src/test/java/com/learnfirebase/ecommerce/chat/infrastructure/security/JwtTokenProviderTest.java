package com.learnfirebase.ecommerce.chat.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import javax.crypto.SecretKey;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;
    private final String testSecret = "myTestSecretKeyForJwtTokenProviderWithMoreThan32Bytes";

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(testSecret);
    }

    @Test
    void shouldValidateValidToken() {
        SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(testSecret.getBytes());
        Date future = new Date(System.currentTimeMillis() + 10000);
        String token = Jwts.builder()
                .subject("user123")
                .claim("type", "access")
                .issuer("ecommerce-auth")
                .audience().add("ecommerce-app").and()
                .expiration(future)
                .signWith(key)
                .compact();

        String parsedUserId = provider.validateAndGetUserId(token);
        assertThat(parsedUserId).isEqualTo("user123");
    }

    @Test
    void shouldRejectInvalidToken() {
        assertThatThrownBy(() -> provider.validateAndGetUserId("invalidTokenString"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectTamperedToken() {
        SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(testSecret.getBytes());
        Date future = new Date(System.currentTimeMillis() + 10000);
        String token = Jwts.builder()
                .subject("user123")
                .claim("type", "access")
                .issuer("ecommerce-auth")
                .audience().add("ecommerce-app").and()
                .expiration(future)
                .signWith(key)
                .compact();

        String tamperedToken = token + "tampered";
        
        assertThatThrownBy(() -> provider.validateAndGetUserId(tamperedToken))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectForWrongSecret() {
        SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(testSecret.getBytes());
        Date future = new Date(System.currentTimeMillis() + 10000);
        String token = Jwts.builder()
                .subject("user123")
                .claim("type", "access")
                .issuer("ecommerce-auth")
                .audience().add("ecommerce-app").and()
                .expiration(future)
                .signWith(key)
                .compact();

        JwtTokenProvider wrongProvider = new JwtTokenProvider("anotherSecretKeyThatIsAlsoMoreThan32BytesLong");
        
        assertThatThrownBy(() -> wrongProvider.validateAndGetUserId(token))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectRefreshToken() {
        SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(testSecret.getBytes());
        Date future = new Date(System.currentTimeMillis() + 10000);
        String token = Jwts.builder()
                .subject("user123")
                .claim("type", "refresh")
                .issuer("ecommerce-auth")
                .audience().add("ecommerce-app").and()
                .expiration(future)
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> provider.validateAndGetUserId(token))
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
