package com.learnfirebase.ecommerce.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Repository;

import com.learnfirebase.ecommerce.common.domain.valueobject.Address;
import com.learnfirebase.ecommerce.common.domain.valueobject.Email;
import com.learnfirebase.ecommerce.identity.application.port.out.UserRepository;
import com.learnfirebase.ecommerce.identity.domain.model.AuthProvider;
import com.learnfirebase.ecommerce.identity.domain.model.HashedPassword;
import com.learnfirebase.ecommerce.identity.domain.model.Role;
import com.learnfirebase.ecommerce.identity.domain.model.User;
import com.learnfirebase.ecommerce.identity.domain.model.UserAddress;
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

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    private UserEntity toEntity(User user) {
        UserEntity entity = UserEntity.builder()
            .id(user.getId().getValue())
            .email(user.getEmail() != null ? user.getEmail().getValue() : null)
            .password(user.getPassword() != null ? user.getPassword().getValue() : null)
            .authProvider(user.getAuthProvider())
            .providerUserId(user.getProviderUserId())
            .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
            .displayName(user.getDisplayName())
            .avatarUrl(user.getAvatarUrl())
            .shopDescription(user.getShopDescription())
            .shopBannerUrl(user.getShopBannerUrl())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
            
        if (user.getAddresses() != null) {
            List<UserAddressEntity> addressEntities = user.getAddresses().stream()
                .map(addr -> toAddressEntity(addr, entity))
                .collect(Collectors.toList());
            entity.setAddresses(addressEntities);
        }
        
        return entity;
    }

    private User toDomain(UserEntity entity) {
        return User.builder()
            .id(new UserId(entity.getId()))
            .email(entity.getEmail() != null ? new Email(entity.getEmail()) : null)
            .password(entity.getPassword() != null ? new HashedPassword(entity.getPassword()) : null)
            .authProvider(entity.getAuthProvider())
            .providerUserId(entity.getProviderUserId())
            .roles(entity.getRoles().stream().map(Role::valueOf).collect(Collectors.toSet()))
            .displayName(entity.getDisplayName())
            .avatarUrl(entity.getAvatarUrl())
            .shopDescription(entity.getShopDescription())
            .shopBannerUrl(entity.getShopBannerUrl())
            .addresses(entity.getAddresses().stream()
                .map(addr -> toAddressDomain(addr))
                .collect(Collectors.toList()))
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    private UserAddressEntity toAddressEntity(UserAddress domain, UserEntity userEntity) {
        return UserAddressEntity.builder()
            .id(domain.getId())
            .user(userEntity)
            .label(domain.getLabel())
            .isDefault(domain.isDefault())
            .fullName(domain.getAddress().getFullName())
            .phoneNumber(domain.getAddress().getPhoneNumber())
            .line1(domain.getAddress().getLine1())
            .line2(domain.getAddress().getLine2())
            .city(domain.getAddress().getCity())
            .state(domain.getAddress().getState())
            .postalCode(domain.getAddress().getPostalCode())
            .country(domain.getAddress().getCountry())
            .build();
    }

    private UserAddress toAddressDomain(UserAddressEntity entity) {
        return UserAddress.builder()
            .id(entity.getId())
            .label(entity.getLabel())
            .isDefault(entity.isDefault())
            .address(Address.builder()
                .fullName(entity.getFullName())
                .phoneNumber(entity.getPhoneNumber())
                .line1(entity.getLine1())
                .line2(entity.getLine2())
                .city(entity.getCity())
                .state(entity.getState())
                .postalCode(entity.getPostalCode())
                .country(entity.getCountry())
                .build())
            .build();
    }
}
