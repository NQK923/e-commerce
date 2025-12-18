package com.learnfirebase.ecommerce.identity.application.service;

import java.time.Instant;
import java.util.UUID;

import com.learnfirebase.ecommerce.common.domain.valueobject.Email;
import com.learnfirebase.ecommerce.identity.application.command.AddAddressCommand;
import com.learnfirebase.ecommerce.identity.application.command.LoginCommand;
import com.learnfirebase.ecommerce.identity.application.command.OAuth2LoginCommand;
import com.learnfirebase.ecommerce.identity.application.command.RegisterUserCommand;
import com.learnfirebase.ecommerce.identity.application.command.ResetPasswordCommand;
import com.learnfirebase.ecommerce.identity.application.command.UpdateProfileCommand;
import com.learnfirebase.ecommerce.identity.application.dto.AuthTokenDto;
import com.learnfirebase.ecommerce.identity.application.dto.UserAddressDto;
import com.learnfirebase.ecommerce.identity.application.dto.UserDto;
import com.learnfirebase.ecommerce.identity.application.port.in.AuthenticateUserUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.ManageUserAddressUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.OAuth2LoginUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.RegisterUserUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.UserQueryUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.UpdateUserProfileUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.ListUsersUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.RequestLoginOtpUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.ResetPasswordUseCase;
import com.learnfirebase.ecommerce.identity.application.port.out.PasswordHasher;
import com.learnfirebase.ecommerce.identity.application.port.out.RefreshTokenRepository;
import com.learnfirebase.ecommerce.identity.application.port.out.TokenProvider;
import com.learnfirebase.ecommerce.identity.application.port.out.UserRepository;
import com.learnfirebase.ecommerce.identity.application.service.OtpService;
import com.learnfirebase.ecommerce.identity.domain.exception.IdentityDomainException;
import com.learnfirebase.ecommerce.identity.domain.model.AuthProvider;
import com.learnfirebase.ecommerce.identity.domain.model.HashedPassword;
import com.learnfirebase.ecommerce.identity.domain.model.User;
import com.learnfirebase.ecommerce.identity.domain.model.UserAddress;
import com.learnfirebase.ecommerce.identity.domain.model.UserId;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IdentityApplicationService implements RegisterUserUseCase, AuthenticateUserUseCase, OAuth2LoginUseCase, UserQueryUseCase, UpdateUserProfileUseCase, ListUsersUseCase, ManageUserAddressUseCase, RequestLoginOtpUseCase, com.learnfirebase.ecommerce.identity.application.port.in.RotateRefreshTokenUseCase, ResetPasswordUseCase {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpService otpService;

    @Override
    public UserDto execute(RegisterUserCommand command) {
        userRepository.findByEmail(command.getEmail()).ifPresent(u -> {
            throw new IdentityDomainException("User already exists");
        });
        if (command.getChallengeId() == null || command.getOtpCode() == null) {
            throw new IdentityDomainException("OTP_REQUIRED");
        }
        otpService.validate(command.getChallengeId(), command.getOtpCode(), null, command.getEmail());

        User user = User.builder()
            .id(new UserId(UUID.randomUUID().toString()))
            .email(new Email(command.getEmail()))
            .password(new HashedPassword(passwordHasher.hash(command.getPassword())))
            .authProvider(AuthProvider.LOCAL)
            .displayName(command.getDisplayName() != null ? command.getDisplayName() : command.getEmail())
            .avatarUrl(null)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        User saved = userRepository.save(user);
        return toDto(saved, command.getDisplayName());
    }

    @Override
    public com.learnfirebase.ecommerce.identity.application.dto.OtpChallengeDto request(String email) {
        return userRepository.findByEmail(email)
            .map(user -> otpService.issue(user.getEmail() != null ? user.getEmail().getValue() : email, user.getId().getValue()))
            .orElseGet(() -> otpService.issue(email, null));
    }

    @Override
    public com.learnfirebase.ecommerce.identity.application.dto.OtpChallengeDto requestReset(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IdentityDomainException("User not found"));
        if (user.getEmail() == null) {
            throw new IdentityDomainException("Email not available for user");
        }
        return otpService.issue(user.getEmail().getValue(), user.getId().getValue());
    }

    @Override
    public void resetPassword(ResetPasswordCommand command) {
        if (command.getChallengeId() == null || command.getOtpCode() == null) {
            throw new IdentityDomainException("OTP_REQUIRED");
        }
        User user = userRepository.findByEmail(command.getEmail())
            .orElseThrow(() -> new IdentityDomainException("User not found"));
        otpService.validate(command.getChallengeId(), command.getOtpCode(), user.getId().getValue(), command.getEmail());
        User updated = User.builder()
            .id(user.getId())
            .email(user.getEmail())
            .password(new HashedPassword(passwordHasher.hash(command.getNewPassword())))
            .authProvider(user.getAuthProvider())
            .providerUserId(user.getProviderUserId())
            .roles(user.getRoles())
            .permissions(user.getPermissions())
            .displayName(user.getDisplayName())
            .avatarUrl(user.getAvatarUrl())
            .createdAt(user.getCreatedAt())
            .updatedAt(Instant.now())
            .addresses(user.getAddresses())
            .build();
        userRepository.save(updated);
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
        return issueTokens(user, command.getDeviceId());
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
                    .avatarUrl(null)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
                return userRepository.save(newUser);
            });

        String emailForToken = user.getEmail() != null ? user.getEmail().getValue() : user.getProviderUserId();
        return issueTokens(user, command.getProviderUserId());
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
            .avatarUrl(command.getAvatarUrl() != null ? command.getAvatarUrl() : user.getAvatarUrl())
            .createdAt(user.getCreatedAt())
            .updatedAt(Instant.now())
            .addresses(user.getAddresses())
            .build();
        User saved = userRepository.save(updated);
        return toDto(saved, null);
    }

    @Override
    public UserAddressDto addAddress(String userId, AddAddressCommand command) {
        User user = userRepository.findById(new UserId(userId))
            .orElseThrow(() -> new IdentityDomainException("User not found"));

        UserAddress newAddress = UserAddress.builder()
            .label(command.getLabel())
            .isDefault(command.isDefault())
            .address(command.getAddress())
            .build();

        if (command.isDefault()) {
            user.getAddresses().forEach(a -> a.setDefault(false));
        }
        
        user.getAddresses().add(newAddress);
        userRepository.save(user);

        return toAddressDto(newAddress);
    }

    @Override
    public void deleteAddress(String userId, String addressId) {
        User user = userRepository.findById(new UserId(userId))
            .orElseThrow(() -> new IdentityDomainException("User not found"));

        boolean removed = user.getAddresses().removeIf(a -> a.getId().equals(addressId));
        if (removed) {
            userRepository.save(user);
        }
    }

    private UserAddressDto toAddressDto(UserAddress domain) {
        return UserAddressDto.builder()
            .id(domain.getId())
            .label(domain.getLabel())
            .isDefault(domain.isDefault())
            .address(domain.getAddress())
            .build();
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
            .avatarUrl(user.getAvatarUrl())
            .createdAt(user.getCreatedAt())
            .addresses(user.getAddresses() != null ? user.getAddresses().stream().map(this::toAddressDto).toList() : java.util.Collections.emptyList())
            .build();
    }

    private AuthTokenDto issueTokens(User user, String deviceId) {
        String emailForToken = user.getEmail() != null ? user.getEmail().getValue() : user.getProviderUserId();
        String accessToken = tokenProvider.generateAccessToken(user.getId().getValue(), emailForToken);
        String refreshToken = tokenProvider.generateRefreshToken(user.getId().getValue(), emailForToken, deviceId);
        String tokenId = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(java.time.Duration.ofDays(30));
        String hash = otpService.hashRaw(refreshToken);
        refreshTokenRepository.save(tokenId, user.getId().getValue(), emailForToken, deviceId, hash, expiresAt);
        return AuthTokenDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    @Override
    public AuthTokenDto rotate(String refreshToken) {
        String hash = otpService.hashRaw(refreshToken);
        var record = refreshTokenRepository.findActiveByToken(hash)
            .orElseThrow(() -> new IdentityDomainException("Invalid refresh token"));
        if (record.expiresAt().isBefore(Instant.now()) || record.revoked()) {
            throw new IdentityDomainException("Expired refresh token");
        }
        refreshTokenRepository.revoke(record.id());
        User user = userRepository.findById(new UserId(record.userId()))
            .orElseThrow(() -> new IdentityDomainException("User not found"));
        return issueTokens(user, record.deviceId());
    }

    @Override
    public void revoke(String refreshToken) {
        String hash = otpService.hashRaw(refreshToken);
        refreshTokenRepository.findActiveByToken(hash).ifPresent(token -> refreshTokenRepository.revoke(token.id()));
    }
}
