package com.learnfirebase.ecommerce.identity.adapter.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnfirebase.ecommerce.identity.application.command.OAuth2LoginCommand;
import com.learnfirebase.ecommerce.identity.application.dto.AuthTokenDto;
import com.learnfirebase.ecommerce.identity.application.port.in.OAuth2LoginUseCase;
import com.learnfirebase.ecommerce.identity.domain.model.AuthProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final OAuth2LoginUseCase oAuth2LoginUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String providerName = String.valueOf(principal.getAttributes().get("auth_provider"));
        String providerUserId = String.valueOf(principal.getAttributes().get("provider_user_id"));
        String email = principal.getAttributes().get("email") != null ? principal.getAttributes().get("email").toString() : null;
        AuthProvider provider = AuthProvider.valueOf(providerName.toUpperCase());

        AuthTokenDto tokens = oAuth2LoginUseCase.execute(OAuth2LoginCommand.builder()
            .provider(provider)
            .providerUserId(providerUserId)
            .email(email)
            .name(String.valueOf(principal.getAttributes().getOrDefault("name", "")))
            .build());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(tokens));
        response.getWriter().flush();
        log.info("OAuth2 login success for provider {} user {}", provider, providerUserId);
    }
}
