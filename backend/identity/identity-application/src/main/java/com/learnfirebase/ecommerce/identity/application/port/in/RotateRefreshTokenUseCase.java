package com.learnfirebase.ecommerce.identity.application.port.in;

import com.learnfirebase.ecommerce.identity.application.dto.AuthTokenDto;

public interface RotateRefreshTokenUseCase {
    AuthTokenDto rotate(String refreshToken);
    void revoke(String refreshToken);
}
