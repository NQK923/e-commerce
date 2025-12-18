package com.learnfirebase.ecommerce.identity.application.port.out;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository {
    void save(String tokenId, String userId, String email, String deviceId, String tokenHash, Instant expiresAt);

    Optional<RefreshTokenRecord> findActiveByToken(String tokenHash);

    void revoke(String tokenId);

    record RefreshTokenRecord(String id, String userId, String email, String deviceId, String tokenHash, Instant expiresAt, boolean revoked) {}
}
