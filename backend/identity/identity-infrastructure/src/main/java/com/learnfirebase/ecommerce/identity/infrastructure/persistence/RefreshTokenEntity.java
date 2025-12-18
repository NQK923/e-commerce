package com.learnfirebase.ecommerce.identity.infrastructure.persistence;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenEntity {
    @Id
    private String id;
    private String userId;
    private String email;
    private String deviceId;
    private String tokenHash;
    private Instant expiresAt;
    private boolean revoked;
    private Instant createdAt;
}
