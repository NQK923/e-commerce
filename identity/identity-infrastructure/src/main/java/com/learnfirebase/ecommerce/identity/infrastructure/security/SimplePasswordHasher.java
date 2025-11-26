package com.learnfirebase.ecommerce.identity.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.identity.application.port.out.PasswordHasher;

@Component
public class SimplePasswordHasher implements PasswordHasher {
    @Override
    public String hash(String rawPassword) {
        return Base64.getEncoder().encodeToString(rawPassword.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean matches(String rawPassword, String hashed) {
        return hash(rawPassword).equals(hashed);
    }
}
