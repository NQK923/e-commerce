package com.learnfirebase.ecommerce.identity.application.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginCommand {
    String email;
    String password;
    String deviceId;
}
