package com.learnfirebase.ecommerce.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.learnfirebase.ecommerce.common.domain.valueobject.Email;
import com.learnfirebase.ecommerce.identity.application.port.out.UserRepository;
import com.learnfirebase.ecommerce.identity.domain.model.AuthProvider;
import com.learnfirebase.ecommerce.identity.domain.model.HashedPassword;
import com.learnfirebase.ecommerce.identity.domain.model.Role;
import com.learnfirebase.ecommerce.identity.domain.model.User;
import com.learnfirebase.ecommerce.identity.domain.model.UserId;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity saved = userJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return userJpaRepository.findById(id.getValue()).map(this::toDomain);
    }

    @Override
    public Optional<User> findByProvider(AuthProvider provider, String providerUserId) {
        return userJpaRepository.findByAuthProviderAndProviderUserId(provider, providerUserId).map(this::toDomain);
    }

    private UserEntity toEntity(User user) {
        return UserEntity.builder()
            .id(user.getId().getValue())
            .email(user.getEmail() != null ? user.getEmail().getValue() : null)
            .password(user.getPassword() != null ? user.getPassword().getValue() : null)
            .authProvider(user.getAuthProvider())
            .providerUserId(user.getProviderUserId())
            .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
            .displayName(user.getDisplayName())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }

    private User toDomain(UserEntity entity) {
        return User.builder()
            .id(new UserId(entity.getId()))
            .email(entity.getEmail() != null ? new Email(entity.getEmail()) : null)
            .password(entity.getPassword() != null ? new HashedPassword(entity.getPassword()) : null)
            .authProvider(entity.getAuthProvider())
            .providerUserId(entity.getProviderUserId())
            .roles(entity.getRoles() != null ? entity.getRoles().stream().map(Role::valueOf).collect(Collectors.toSet()) : java.util.Collections.emptySet())
            .displayName(entity.getDisplayName())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
