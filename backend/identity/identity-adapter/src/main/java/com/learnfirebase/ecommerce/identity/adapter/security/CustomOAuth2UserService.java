package com.learnfirebase.ecommerce.identity.adapter.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.learnfirebase.ecommerce.identity.domain.model.AuthProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());

        AuthProvider provider = mapProvider(userRequest.getClientRegistration().getRegistrationId());
        String providerUserId = extractProviderUserId(provider, attributes, oauth2User.getName());

        String nameAttr = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
                .getUserNameAttributeName();

        attributes.put("auth_provider", provider.name());
        attributes.put("provider_user_id", providerUserId);
        attributes.putIfAbsent("name", attributes.get(nameAttr));
        return new DefaultOAuth2User(oauth2User.getAuthorities(), attributes, nameAttr);
    }

    private AuthProvider mapProvider(String registrationId) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return AuthProvider.GOOGLE;
        }
        if ("facebook".equalsIgnoreCase(registrationId)) {
            return AuthProvider.FACEBOOK;
        }
        return AuthProvider.LOCAL;
    }

    private String extractProviderUserId(AuthProvider provider, Map<String, Object> attributes, String defaultValue) {
        if (provider == AuthProvider.GOOGLE) {
            return attributes.getOrDefault("sub", defaultValue).toString();
        }
        if (provider == AuthProvider.FACEBOOK) {
            return attributes.getOrDefault("id", defaultValue).toString();
        }
        return defaultValue;
    }
}
