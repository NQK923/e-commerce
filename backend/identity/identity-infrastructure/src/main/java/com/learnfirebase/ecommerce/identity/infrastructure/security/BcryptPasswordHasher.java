package com.learnfirebase.ecommerce.identity.infrastructure.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.identity.application.port.out.PasswordHasher;

@Component
public class BcryptPasswordHasher implements PasswordHasher {
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hashed) {
        return passwordEncoder.matches(rawPassword, hashed);
    }
}
