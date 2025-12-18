package com.learnfirebase.ecommerce.identity.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.learnfirebase.ecommerce.identity.application.port.out.PasswordHasher;
import com.learnfirebase.ecommerce.identity.application.port.out.TokenProvider;
import com.learnfirebase.ecommerce.identity.application.port.out.UserRepository;
import com.learnfirebase.ecommerce.identity.application.port.out.RefreshTokenRepository;
import com.learnfirebase.ecommerce.identity.application.service.IdentityApplicationService;
import com.learnfirebase.ecommerce.identity.application.service.OtpService;
import com.learnfirebase.ecommerce.identity.application.service.SellerApplicationService;
import com.learnfirebase.ecommerce.identity.infrastructure.persistence.UserEntity;
import com.learnfirebase.ecommerce.identity.infrastructure.persistence.UserJpaRepository;
import com.learnfirebase.ecommerce.identity.infrastructure.persistence.OtpTokenEntity;
import com.learnfirebase.ecommerce.identity.infrastructure.persistence.OtpTokenJpaRepository;
import com.learnfirebase.ecommerce.identity.infrastructure.persistence.RefreshTokenEntity;
import com.learnfirebase.ecommerce.identity.infrastructure.persistence.RefreshTokenJpaRepository;
import com.learnfirebase.ecommerce.identity.application.port.out.SellerApplicationRepository;
import com.learnfirebase.ecommerce.identity.application.port.out.OtpRepository;
import com.learnfirebase.ecommerce.identity.application.port.out.OtpSender;

@Configuration
@EnableJpaRepositories(basePackageClasses = {UserJpaRepository.class, OtpTokenJpaRepository.class, RefreshTokenJpaRepository.class})
@EntityScan(basePackageClasses = {UserEntity.class, OtpTokenEntity.class, RefreshTokenEntity.class})
public class IdentityModuleConfig {
    @Bean
    public IdentityApplicationService identityApplicationService(UserRepository userRepository, PasswordHasher passwordHasher, TokenProvider tokenProvider, RefreshTokenRepository refreshTokenRepository, OtpService otpService) {
        return new IdentityApplicationService(userRepository, passwordHasher, tokenProvider, refreshTokenRepository, otpService);
    }

    @Bean
    public OtpService otpService(OtpRepository otpRepository, OtpSender otpSender) {
        return new OtpService(otpRepository, otpSender);
    }

    @Bean
    public SellerApplicationService sellerApplicationService(SellerApplicationRepository sellerApplicationRepository, UserRepository userRepository) {
        return new SellerApplicationService(sellerApplicationRepository, userRepository);
    }

    @Bean
    @Profile("!test")
    public StartupAdminSeeder startupAdminSeeder(UserRepository userRepository, PasswordHasher passwordHasher) {
        return new StartupAdminSeeder(userRepository, passwordHasher);
    }
}
