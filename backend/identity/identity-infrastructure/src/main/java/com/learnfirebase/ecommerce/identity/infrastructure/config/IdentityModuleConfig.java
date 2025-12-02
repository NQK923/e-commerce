package com.learnfirebase.ecommerce.identity.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.learnfirebase.ecommerce.identity.application.port.out.PasswordHasher;
import com.learnfirebase.ecommerce.identity.application.port.out.TokenProvider;
import com.learnfirebase.ecommerce.identity.application.port.out.UserRepository;
import com.learnfirebase.ecommerce.identity.application.service.IdentityApplicationService;
import com.learnfirebase.ecommerce.identity.infrastructure.persistence.UserEntity;
import com.learnfirebase.ecommerce.identity.infrastructure.persistence.UserJpaRepository;

@Configuration
@EnableJpaRepositories(basePackageClasses = UserJpaRepository.class)
@EntityScan(basePackageClasses = UserEntity.class)
public class IdentityModuleConfig {
    @Bean
    public IdentityApplicationService identityApplicationService(UserRepository userRepository, PasswordHasher passwordHasher, TokenProvider tokenProvider) {
        return new IdentityApplicationService(userRepository, passwordHasher, tokenProvider);
    }

    @Bean
    @Profile("!test")
    public StartupAdminSeeder startupAdminSeeder(UserRepository userRepository, PasswordHasher passwordHasher) {
        return new StartupAdminSeeder(userRepository, passwordHasher);
    }
}
