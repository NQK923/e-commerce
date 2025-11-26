package com.learnfirebase.ecommerce.identity.application.service;

import java.time.Instant;
import java.util.UUID;

import com.learnfirebase.ecommerce.common.domain.valueobject.Email;
import com.learnfirebase.ecommerce.identity.application.command.LoginCommand;
import com.learnfirebase.ecommerce.identity.application.command.RegisterUserCommand;
import com.learnfirebase.ecommerce.identity.application.dto.AuthTokenDto;
import com.learnfirebase.ecommerce.identity.application.dto.UserDto;
import com.learnfirebase.ecommerce.identity.application.port.in.AuthenticateUserUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.RegisterUserUseCase;
import com.learnfirebase.ecommerce.identity.application.port.out.PasswordHasher;
import com.learnfirebase.ecommerce.identity.application.port.out.TokenProvider;
import com.learnfirebase.ecommerce.identity.application.port.out.UserRepository;
import com.learnfirebase.ecommerce.identity.domain.exception.IdentityDomainException;
import com.learnfirebase.ecommerce.identity.domain.model.HashedPassword;
import com.learnfirebase.ecommerce.identity.domain.model.User;
import com.learnfirebase.ecommerce.identity.domain.model.UserId;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IdentityApplicationService implements RegisterUserUseCase, AuthenticateUserUseCase {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    @Override
    public UserDto execute(RegisterUserCommand command) {
        userRepository.findByEmail(command.getEmail()).ifPresent(u -> {
            throw new IdentityDomainException("User already exists");
        });

        User user = User.builder()
            .id(new UserId(UUID.randomUUID().toString()))
            .email(new Email(command.getEmail()))
            .password(new HashedPassword(passwordHasher.hash(command.getPassword())))
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        User saved = userRepository.save(user);
        return UserDto.builder()
            .id(saved.getId().getValue())
            .email(saved.getEmail().getValue())
            .roles(saved.getRoles().stream().map(Enum::name).toList())
            .createdAt(saved.getCreatedAt())
            .build();
    }

    @Override
    public AuthTokenDto execute(LoginCommand command) {
        User user = userRepository.findByEmail(command.getEmail())
            .orElseThrow(() -> new IdentityDomainException("User not found"));
        if (!passwordHasher.matches(command.getPassword(), user.getPassword().getValue())) {
            throw new IdentityDomainException("Invalid credentials");
        }
        return AuthTokenDto.builder()
            .accessToken(tokenProvider.generateAccessToken(user.getId().getValue(), user.getEmail().getValue()))
            .refreshToken(tokenProvider.generateRefreshToken(user.getId().getValue(), user.getEmail().getValue(), command.getDeviceId()))
            .build();
    }
}
