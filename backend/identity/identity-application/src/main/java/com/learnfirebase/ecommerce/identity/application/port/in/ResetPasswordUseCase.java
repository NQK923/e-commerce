package com.learnfirebase.ecommerce.identity.application.port.in;

import com.learnfirebase.ecommerce.identity.application.command.ResetPasswordCommand;
import com.learnfirebase.ecommerce.identity.application.dto.OtpChallengeDto;

public interface ResetPasswordUseCase {
    OtpChallengeDto requestReset(String email);
    void resetPassword(ResetPasswordCommand command);
}
