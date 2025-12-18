package com.learnfirebase.ecommerce.identity.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class ResetPasswordCommand {
    String email;
    String newPassword;
    String otpCode;
    String challengeId;
}
