package com.learnfirebase.ecommerce.identity.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.learnfirebase.ecommerce.identity.application.port.out.PasswordHasher;
import com.learnfirebase.ecommerce.identity.application.port.out.TokenProvider;
import com.learnfirebase.ecommerce.identity.application.port.out.UserRepository;
import com.learnfirebase.ecommerce.identity.application.service.IdentityApplicationService;

@Configuration
public class IdentityModuleConfig {
    @Bean
    public IdentityApplicationService identityApplicationService(UserRepository userRepository, PasswordHasher passwordHasher, TokenProvider tokenProvider) {
        return new IdentityApplicationService(userRepository, passwordHasher, tokenProvider);
    }
}
