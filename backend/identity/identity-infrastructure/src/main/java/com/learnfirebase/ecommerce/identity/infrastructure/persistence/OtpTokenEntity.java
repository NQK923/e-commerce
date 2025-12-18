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
@Table(name = "otp_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpTokenEntity {
    @Id
    private String id;
    private String email;
    private String userId;
    private String codeHash;
    private Instant expiresAt;
    private boolean used;
    private int attempts;
    private Instant createdAt;
}
