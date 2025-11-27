package com.learnfirebase.ecommerce.identity.application.port.out;

public interface TokenProvider {
    String generateAccessToken(String userId, String email);

    String generateRefreshToken(String userId, String email, String deviceId);
}
