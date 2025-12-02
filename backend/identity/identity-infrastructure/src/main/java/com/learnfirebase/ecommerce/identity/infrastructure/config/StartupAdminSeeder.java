package com.learnfirebase.ecommerce.identity.infrastructure.config;

import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import com.learnfirebase.ecommerce.common.domain.valueobject.Email;
import com.learnfirebase.ecommerce.identity.application.port.out.PasswordHasher;
import com.learnfirebase.ecommerce.identity.application.port.out.UserRepository;
import com.learnfirebase.ecommerce.identity.domain.model.AuthProvider;
import com.learnfirebase.ecommerce.identity.domain.model.HashedPassword;
import com.learnfirebase.ecommerce.identity.domain.model.Role;
import com.learnfirebase.ecommerce.identity.domain.model.User;
import com.learnfirebase.ecommerce.identity.domain.model.UserId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class StartupAdminSeeder implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Override
    public void run(ApplicationArguments args) {
        String adminEmail = "nqk1337@gmail.com";
        userRepository.findByEmail(adminEmail).ifPresentOrElse(
            existing -> log.info("Admin seed skipped; user {} already exists", adminEmail),
            () -> {
                User admin = User.builder()
                    .id(new UserId(UUID.randomUUID().toString()))
                    .email(new Email(adminEmail))
                    .password(new HashedPassword(passwordHasher.hash("Admin@123")))
                    .authProvider(AuthProvider.LOCAL)
                    .roles(EnumSet.of(Role.ADMIN))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
                userRepository.save(admin);
                log.info("Seeded admin account {}", adminEmail);
            });
    }
}
