package com.learnfirebase.ecommerce.identity.application.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterUserCommand {
    String email;
    String password;
    String displayName;
    String deviceId;
}
