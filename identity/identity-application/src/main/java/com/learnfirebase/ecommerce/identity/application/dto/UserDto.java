package com.learnfirebase.ecommerce.identity.application.dto;

import java.time.Instant;
import java.util.Set;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class UserDto {
    String id;
    String email;
    @Singular
    Set<String> roles;
    Instant createdAt;
}
