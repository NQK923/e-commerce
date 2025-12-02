package com.learnfirebase.ecommerce.identity.adapter.web;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.identity.application.command.LoginCommand;
import com.learnfirebase.ecommerce.identity.application.command.RegisterUserCommand;
import com.learnfirebase.ecommerce.identity.application.dto.AuthTokenDto;
import com.learnfirebase.ecommerce.identity.application.dto.UserDto;
import com.learnfirebase.ecommerce.identity.application.port.in.AuthenticateUserUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.RegisterUserUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.UserQueryUseCase;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final UserQueryUseCase userQueryUseCase;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterUserCommand command) {
        UserDto user = registerUserUseCase.execute(command);
        AuthTokenDto tokens = authenticateUserUseCase.execute(LoginCommand.builder()
            .email(command.getEmail())
            .password(command.getPassword())
            .deviceId(command.getDeviceId())
            .build());
        return ResponseEntity.ok(toResponse(user, tokens));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginCommand command) {
        AuthTokenDto tokens = authenticateUserUseCase.execute(command);
        UserDto user = userQueryUseCase.getByEmail(command.getEmail());
        return ResponseEntity.ok(toResponse(user, tokens));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUserResponse> me(@RequestHeader(name = "Authorization", required = false) String authorization) {
        String email = extractEmailFromAccessToken(authorization);
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        UserDto user = userQueryUseCase.getByEmail(email);
        return ResponseEntity.ok(toUserResponse(user));
    }

    private AuthResponse toResponse(UserDto user, AuthTokenDto tokens) {
        return AuthResponse.builder()
            .user(toUserResponse(user))
            .accessToken(tokens.getAccessToken())
            .refreshToken(tokens.getRefreshToken())
            .build();
    }

    private AuthUserResponse toUserResponse(UserDto user) {
        return AuthUserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .displayName(user.getDisplayName())
            .provider(user.getProvider() != null ? user.getProvider().name() : null)
            .roles(user.getRoles())
            .build();
    }

    private String extractEmailFromAccessToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authorization.substring(7);
            String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length >= 2) {
                return parts[1];
            }
        } catch (IllegalArgumentException ignored) {
            // invalid token
        }
        return null;
    }

    @Value
    @Builder
    static class AuthResponse {
        AuthUserResponse user;
        String accessToken;
        String refreshToken;
    }

    @Value
    @Builder
    static class AuthUserResponse {
        String id;
        String email;
        String displayName;
        String provider;
        Set<String> roles;
    }
}
