package com.learnfirebase.ecommerce.identity.domain.model;

import java.time.Instant;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RefreshToken {
    String token;
    Instant expiresAt;
}
