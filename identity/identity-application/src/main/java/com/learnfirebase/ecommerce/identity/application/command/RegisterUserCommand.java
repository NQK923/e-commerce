package com.learnfirebase.ecommerce.identity.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegisterUserCommand {
    String email;
    String password;
}
