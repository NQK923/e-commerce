package com.learnfirebase.ecommerce.identity.application.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthLoginResultDto {
    boolean mfaRequired;
    String challengeId;
    Instant challengeExpiresAt;
    String maskedEmail;

    String accessToken;
    String refreshToken;
}
