package com.learnfirebase.ecommerce.identity.application.dto;

import java.time.Instant;
import java.util.Set;

import com.learnfirebase.ecommerce.identity.domain.model.AuthProvider;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class UserDto {
    String id;
    String email;
    String displayName;
    AuthProvider provider;
    @Singular
    Set<String> roles;
    Instant createdAt;
}
