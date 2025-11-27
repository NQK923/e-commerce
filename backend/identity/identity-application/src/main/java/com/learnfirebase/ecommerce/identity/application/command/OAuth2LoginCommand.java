package com.learnfirebase.ecommerce.identity.application.command;

import com.learnfirebase.ecommerce.identity.domain.model.AuthProvider;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OAuth2LoginCommand {
    AuthProvider provider;
    String providerUserId;
    String email;
    String name;
}
