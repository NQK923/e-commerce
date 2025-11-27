package com.learnfirebase.ecommerce.identity.application.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthTokenDto {
    String accessToken;
    String refreshToken;
}
