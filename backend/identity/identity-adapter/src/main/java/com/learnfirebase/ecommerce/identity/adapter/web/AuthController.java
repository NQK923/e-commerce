package com.learnfirebase.ecommerce.identity.adapter.web;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.identity.application.command.LoginCommand;
import com.learnfirebase.ecommerce.identity.application.command.OAuth2LoginCommand;
import com.learnfirebase.ecommerce.identity.application.command.RegisterUserCommand;
import com.learnfirebase.ecommerce.identity.application.command.ResetPasswordCommand;
import com.learnfirebase.ecommerce.identity.application.dto.AuthTokenDto;
import com.learnfirebase.ecommerce.identity.application.dto.OtpChallengeDto;
import com.learnfirebase.ecommerce.identity.application.dto.UserDto;
import com.learnfirebase.ecommerce.identity.application.port.in.AuthenticateUserUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.OAuth2LoginUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.RequestLoginOtpUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.RotateRefreshTokenUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.RegisterUserUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.ResetPasswordUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.UserQueryUseCase;
import com.learnfirebase.ecommerce.identity.domain.exception.IdentityDomainException;
import com.learnfirebase.ecommerce.identity.domain.model.AuthProvider;

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
    private final OAuth2LoginUseCase oAuth2LoginUseCase;

    @org.springframework.beans.factory.annotation.Value("${identity.oauth2.dev-callback-enabled:false}")
    private boolean devOAuth2CallbackEnabled;

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
    public ResponseEntity<AuthUserResponse> me(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        String userId = authentication.getPrincipal().toString();
        UserDto user = userQueryUseCase.getById(userId);
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

    @PostMapping("/oauth2/callback")
    public ResponseEntity<?> completeOAuth2Callback(@RequestBody OAuthCallbackRequest request) {
        if (!devOAuth2CallbackEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ErrorResponse.builder()
                .code("OAUTH_PROVIDER_CALLBACK_REQUIRED")
                .message("OAuth2 provider callback is not enabled. Use /oauth2/authorization/{provider} or enable the local dev callback.")
                .build());
        }

        if (request.getProvider() == null || request.getProvider().isBlank()) {
            return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .code("OAUTH_PROVIDER_REQUIRED")
                .message("OAuth provider is required")
                .build());
        }

        try {
            AuthProvider provider = AuthProvider.valueOf(request.getProvider().trim().toUpperCase());
            String providerUserId = firstNonBlank(request.getProviderUserId(), request.getCode(), request.getToken());
            if (providerUserId == null) {
                return ResponseEntity.badRequest().body(ErrorResponse.builder()
                    .code("OAUTH_PROVIDER_USER_REQUIRED")
                    .message("OAuth provider user id, code, or token is required")
                    .build());
            }

            if (request.getEmail() == null || request.getEmail().isBlank()) {
                return ResponseEntity.badRequest().body(ErrorResponse.builder()
                    .code("OAUTH_EMAIL_REQUIRED")
                    .message("Email is required from OAuth provider")
                    .build());
            }

            AuthTokenDto tokens = oAuth2LoginUseCase.execute(OAuth2LoginCommand.builder()
                .provider(provider)
                .providerUserId(providerUserId)
                .email(request.getEmail())
                .name(request.getName())
                .build());
            
            UserDto user = userQueryUseCase.getByEmail(request.getEmail());
            
            return ResponseEntity.ok(toResponse(user, tokens));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .code("OAUTH_PROVIDER_UNSUPPORTED")
                .message("OAuth provider is not supported")
                .build());
        } catch (IdentityDomainException ex) {
            return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .code("OAUTH_LOGIN_FAILED")
                .message(ex.getMessage())
                .build());
        }
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
        if (user == null) return null;
        return AuthUserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .displayName(user.getDisplayName())
            .avatarUrl(user.getAvatarUrl())
            .provider(user.getProvider() != null ? user.getProvider().name() : null)
            .roles(user.getRoles())
            .build();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
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

    @Data
    @NoArgsConstructor
    static class OAuthCallbackRequest {
        private String provider;
        private String providerUserId;
        private String email;
        private String name;
        private String code;
        private String state;
        private String token;
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
