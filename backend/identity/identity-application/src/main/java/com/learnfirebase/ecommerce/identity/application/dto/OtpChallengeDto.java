package com.learnfirebase.ecommerce.identity.application.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OtpChallengeDto {
    String id;
    String email;
    Instant expiresAt;
}
