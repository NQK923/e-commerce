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
import com.learnfirebase.ecommerce.identity.application.command.ResetPasswordCommand;
import com.learnfirebase.ecommerce.identity.application.dto.AuthTokenDto;
import com.learnfirebase.ecommerce.identity.application.dto.OtpChallengeDto;
import com.learnfirebase.ecommerce.identity.application.dto.UserDto;
import com.learnfirebase.ecommerce.identity.application.port.in.AuthenticateUserUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.RequestLoginOtpUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.RotateRefreshTokenUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.RegisterUserUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.ResetPasswordUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.UserQueryUseCase;
import com.learnfirebase.ecommerce.identity.domain.exception.IdentityDomainException;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RequestLoginOtpUseCase requestLoginOtpUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final UserQueryUseCase userQueryUseCase;
    private final RotateRefreshTokenUseCase rotateRefreshTokenUseCase;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserCommand command) {
        try {
            UserDto user = registerUserUseCase.execute(command);
            AuthTokenDto tokens = authenticateUserUseCase.execute(LoginCommand.builder()
                .email(command.getEmail())
                .password(command.getPassword())
                .deviceId(command.getDeviceId())
                .skipOtp(true)
                .build());
            return ResponseEntity.ok(toResponse(user, tokens));
        } catch (IdentityDomainException ex) {
            if ("OTP_REQUIRED".equals(ex.getMessage())) {
                return ResponseEntity.status(428).body(ErrorResponse.builder()
                    .code("OTP_REQUIRED")
                    .message("OTP required to verify email for registration.")
                    .build());
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginCommand command) {
        try {
            AuthTokenDto tokens = authenticateUserUseCase.execute(command);
            UserDto user = userQueryUseCase.getByEmail(command.getEmail());
            return ResponseEntity.ok(toResponse(user, tokens));
        } catch (IdentityDomainException ex) {
            return ResponseEntity.status(401).body(ErrorResponse.builder()
                .code("INVALID_CREDENTIALS")
                .message("Email or password is incorrect")
                .build());
        }
    }

    @PostMapping("/otp/request")
    public ResponseEntity<OtpChallengeDto> requestOtp(@RequestBody OtpRequest request) {
        OtpChallengeDto challenge = requestLoginOtpUseCase.request(request.getEmail());
        return ResponseEntity.accepted().body(challenge);
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<OtpChallengeDto> forgotPassword(@RequestBody EmailRequest request) {
        try {
            OtpChallengeDto challenge = resetPasswordUseCase.requestReset(request.getEmail());
            return ResponseEntity.accepted().body(challenge);
        } catch (IdentityDomainException ex) {
            // Avoid leaking whether the email exists; return accepted without details.
            return ResponseEntity.accepted().build();
        }
    }

    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            resetPasswordUseCase.resetPassword(ResetPasswordCommand.builder()
                .email(request.getEmail())
                .newPassword(request.getNewPassword())
                .challengeId(request.getChallengeId())
                .otpCode(request.getOtpCode())
                .build());
            return ResponseEntity.noContent().build();
        } catch (IdentityDomainException ex) {
            return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .code("RESET_FAILED")
                .message(ex.getMessage())
                .build());
        }
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

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthTokenDto> refresh(@RequestBody RefreshRequest request) {
        try {
            return ResponseEntity.ok(rotateRefreshTokenUseCase.rotate(request.getRefreshToken()));
        } catch (IdentityDomainException ex) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
        if (request.getRefreshToken() != null) {
            rotateRefreshTokenUseCase.revoke(request.getRefreshToken());
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logoutGet() {
        return ResponseEntity.noContent().build();
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
            .avatarUrl(user.getAvatarUrl())
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

    @Data
    @NoArgsConstructor
    private static class RefreshRequest {
        private String refreshToken;
    }

    @Data
    @NoArgsConstructor
    private static class OtpRequest {
        private String email;
    }

    @Data
    @NoArgsConstructor
    private static class EmailRequest {
        private String email;
    }

    @Data
    @NoArgsConstructor
    private static class ResetPasswordRequest {
        private String email;
        private String newPassword;
        private String otpCode;
        private String challengeId;
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
        String avatarUrl;
        String provider;
        Set<String> roles;
    }

    @Value
    @Builder
    static class ErrorResponse {
        String code;
        String message;
    }
}
