package com.learnfirebase.ecommerce.identity.application.port.in;

import com.learnfirebase.ecommerce.identity.application.dto.OtpChallengeDto;

public interface RequestLoginOtpUseCase {
    OtpChallengeDto request(String email);
}
