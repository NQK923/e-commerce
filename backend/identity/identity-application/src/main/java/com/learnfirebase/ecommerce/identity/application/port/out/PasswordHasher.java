package com.learnfirebase.ecommerce.identity.application.port.out;

public interface PasswordHasher {
    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashed);
}
