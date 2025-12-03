package com.learnfirebase.ecommerce.identity.application.service;

import java.time.Instant;
import java.util.UUID;

import com.learnfirebase.ecommerce.common.domain.valueobject.Email;
import com.learnfirebase.ecommerce.identity.application.command.LoginCommand;
import com.learnfirebase.ecommerce.identity.application.command.OAuth2LoginCommand;
import com.learnfirebase.ecommerce.identity.application.command.RegisterUserCommand;
import com.learnfirebase.ecommerce.identity.application.command.UpdateProfileCommand;
import com.learnfirebase.ecommerce.identity.application.dto.AuthTokenDto;
import com.learnfirebase.ecommerce.identity.application.dto.UserDto;
import com.learnfirebase.ecommerce.identity.application.port.in.AuthenticateUserUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.OAuth2LoginUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.RegisterUserUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.UserQueryUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.UpdateUserProfileUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.ListUsersUseCase;
import com.learnfirebase.ecommerce.identity.application.port.out.PasswordHasher;
import com.learnfirebase.ecommerce.identity.application.port.out.TokenProvider;
import com.learnfirebase.ecommerce.identity.application.port.out.UserRepository;
import com.learnfirebase.ecommerce.identity.domain.exception.IdentityDomainException;
import com.learnfirebase.ecommerce.identity.domain.model.AuthProvider;
import com.learnfirebase.ecommerce.identity.domain.model.HashedPassword;
import com.learnfirebase.ecommerce.identity.domain.model.User;
import com.learnfirebase.ecommerce.identity.domain.model.UserId;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IdentityApplicationService implements RegisterUserUseCase, AuthenticateUserUseCase, OAuth2LoginUseCase, UserQueryUseCase, UpdateUserProfileUseCase, ListUsersUseCase {
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
            .authProvider(AuthProvider.LOCAL)
            .displayName(command.getDisplayName() != null ? command.getDisplayName() : command.getEmail())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        User saved = userRepository.save(user);
        return toDto(saved, command.getDisplayName());
    }

    @Override
    public AuthTokenDto execute(LoginCommand command) {
        User user = userRepository.findByEmail(command.getEmail())
            .orElseThrow(() -> new IdentityDomainException("User not found"));
        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new IdentityDomainException("User must login via " + user.getAuthProvider());
        }
        if (!passwordHasher.matches(command.getPassword(), user.getPassword().getValue())) {
            throw new IdentityDomainException("Invalid credentials");
        }
        return AuthTokenDto.builder()
            .accessToken(tokenProvider.generateAccessToken(user.getId().getValue(), user.getEmail().getValue()))
            .refreshToken(tokenProvider.generateRefreshToken(user.getId().getValue(), user.getEmail().getValue(), command.getDeviceId()))
            .build();
    }

    @Override
    public AuthTokenDto execute(OAuth2LoginCommand command) {
        AuthProvider provider = command.getProvider();
        String providerUserId = command.getProviderUserId();
        if (provider == null || providerUserId == null) {
            throw new IdentityDomainException("Provider and provider user id are required for OAuth2 login");
        }
        User user = userRepository.findByProvider(provider, providerUserId)
            .orElseGet(() -> {
                String email = command.getEmail();
                User newUser = User.builder()
                    .id(new UserId(UUID.randomUUID().toString()))
                    .email(email != null ? new Email(email) : null)
                    .password(new HashedPassword(""))
                    .authProvider(provider)
                    .providerUserId(providerUserId)
                    .displayName(email != null ? email : providerUserId)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
                return userRepository.save(newUser);
            });

        String emailForToken = user.getEmail() != null ? user.getEmail().getValue() : user.getProviderUserId();
        return AuthTokenDto.builder()
            .accessToken(tokenProvider.generateAccessToken(user.getId().getValue(), emailForToken))
            .refreshToken(tokenProvider.generateRefreshToken(user.getId().getValue(), emailForToken, command.getProviderUserId()))
            .build();
    }

    @Override
    public UserDto getByEmail(String email) {
        return userRepository.findByEmail(email)
            .map(user -> toDto(user, null))
            .orElseThrow(() -> new IdentityDomainException("User not found"));
    }

    @Override
    public UserDto getById(String id) {
        return userRepository.findById(new UserId(id))
            .map(user -> toDto(user, null))
            .orElseThrow(() -> new IdentityDomainException("User not found"));
    }

    @Override
    public java.util.List<UserDto> list() {
        return userRepository.findAll().stream()
            .map(user -> toDto(user, null))
            .toList();
    }

    @Override
    public UserDto execute(UpdateProfileCommand command) {
        User user = userRepository.findById(new UserId(command.getUserId()))
            .orElseGet(() -> userRepository.findByEmail(command.getEmail())
                .orElseThrow(() -> new IdentityDomainException("User not found")));
        String displayName = command.getDisplayName() != null ? command.getDisplayName() : user.getDisplayName();
        User updated = User.builder()
            .id(user.getId())
            .email(user.getEmail())
            .password(user.getPassword())
            .authProvider(user.getAuthProvider())
            .providerUserId(user.getProviderUserId())
            .roles(user.getRoles())
            .permissions(user.getPermissions())
            .displayName(displayName)
            .createdAt(user.getCreatedAt())
            .updatedAt(Instant.now())
            .build();
        User saved = userRepository.save(updated);
        return toDto(saved, null);
    }

    private UserDto toDto(User user, String displayNameOverride) {
        String displayName = displayNameOverride != null ? displayNameOverride
            : (user.getDisplayName() != null ? user.getDisplayName()
                : (user.getEmail() != null ? user.getEmail().getValue() : user.getProviderUserId()));
        return UserDto.builder()
            .id(user.getId().getValue())
            .email(user.getEmail() != null ? user.getEmail().getValue() : null)
            .displayName(displayName)
            .provider(user.getAuthProvider())
            .roles(user.getRoles().stream().map(Enum::name).toList())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
