package com.learnfirebase.ecommerce.identity.application.port.out;

import java.time.Instant;
import java.util.Optional;

public interface OtpRepository {
    String save(String email, String userId, String codeHash, Instant expiresAt);

    Optional<OtpRecord> findById(String id);

    void markUsed(String id);

    void incrementAttempts(String id);

    record OtpRecord(String id, String email, String userId, String codeHash, Instant expiresAt, boolean used, int attempts) {}
}
