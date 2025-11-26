package com.learnfirebase.ecommerce.identity.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginCommand {
    String email;
    String password;
    String deviceId;
}
