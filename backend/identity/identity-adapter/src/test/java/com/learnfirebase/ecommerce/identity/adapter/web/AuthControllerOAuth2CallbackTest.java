package com.learnfirebase.ecommerce.identity.adapter.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import com.learnfirebase.ecommerce.identity.application.command.OAuth2LoginCommand;
import com.learnfirebase.ecommerce.identity.application.dto.AuthTokenDto;
import com.learnfirebase.ecommerce.identity.application.dto.UserDto;
import com.learnfirebase.ecommerce.identity.application.port.in.AuthenticateUserUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.OAuth2LoginUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.RegisterUserUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.RequestLoginOtpUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.ResetPasswordUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.RotateRefreshTokenUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.UserQueryUseCase;
import com.learnfirebase.ecommerce.identity.domain.model.AuthProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthControllerOAuth2CallbackTest {
    @Mock
    private AuthenticateUserUseCase authenticateUserUseCase;
    @Mock
    private RequestLoginOtpUseCase requestLoginOtpUseCase;
    @Mock
    private RegisterUserUseCase registerUserUseCase;
    @Mock
    private ResetPasswordUseCase resetPasswordUseCase;
    @Mock
    private UserQueryUseCase userQueryUseCase;
    @Mock
    private RotateRefreshTokenUseCase rotateRefreshTokenUseCase;
    @Mock
    private OAuth2LoginUseCase oAuth2LoginUseCase;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(
            authenticateUserUseCase,
            requestLoginOtpUseCase,
            registerUserUseCase,
            resetPasswordUseCase,
            userQueryUseCase,
            rotateRefreshTokenUseCase,
            oAuth2LoginUseCase
        );
    }

    @Test
    void devOAuth2CallbackIsDisabledByDefault() {
        AuthController.OAuthCallbackRequest request = request();

        var response = controller.completeOAuth2Callback(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
        verify(oAuth2LoginUseCase, never()).execute(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void devOAuth2CallbackIssuesFrontendAuthResponseWhenEnabled() {
        ReflectionTestUtils.setField(controller, "devOAuth2CallbackEnabled", true);
        when(oAuth2LoginUseCase.execute(org.mockito.ArgumentMatchers.any(OAuth2LoginCommand.class)))
            .thenReturn(AuthTokenDto.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build());
        when(userQueryUseCase.getByEmail("local.google@example.local"))
            .thenReturn(UserDto.builder()
                .id("user-1")
                .email("local.google@example.local")
                .displayName("Local Google")
                .provider(AuthProvider.GOOGLE)
                .roles(Set.of("BUYER"))
                .build());

        var response = controller.completeOAuth2Callback(request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(AuthController.AuthResponse.class);
        AuthController.AuthResponse body = (AuthController.AuthResponse) response.getBody();
        assertThat(body.getAccessToken()).isEqualTo("access-token");
        assertThat(body.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(body.getUser().getEmail()).isEqualTo("local.google@example.local");
        assertThat(body.getUser().getProvider()).isEqualTo("GOOGLE");

        ArgumentCaptor<OAuth2LoginCommand> commandCaptor = ArgumentCaptor.forClass(OAuth2LoginCommand.class);
        verify(oAuth2LoginUseCase).execute(commandCaptor.capture());
        assertThat(commandCaptor.getValue().getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(commandCaptor.getValue().getProviderUserId()).isEqualTo("local-google-user");
        assertThat(commandCaptor.getValue().getEmail()).isEqualTo("local.google@example.local");
    }

    @Test
    void devOAuth2CallbackRejectsIfEmailIsBlank() {
        ReflectionTestUtils.setField(controller, "devOAuth2CallbackEnabled", true);
        AuthController.OAuthCallbackRequest request = request();
        request.setEmail("");

        var response = controller.completeOAuth2Callback(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(oAuth2LoginUseCase, never()).execute(org.mockito.ArgumentMatchers.any());
    }

    private AuthController.OAuthCallbackRequest request() {
        AuthController.OAuthCallbackRequest request = new AuthController.OAuthCallbackRequest();
        request.setProvider("google");
        request.setProviderUserId("local-google-user");
        request.setEmail("local.google@example.local");
        request.setName("Local Google");
        return request;
    }
}
