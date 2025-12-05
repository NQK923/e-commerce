package com.learnfirebase.ecommerce.identity.application.service;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.learnfirebase.ecommerce.common.domain.valueobject.Email;
import com.learnfirebase.ecommerce.identity.application.command.ReviewSellerApplicationCommand;
import com.learnfirebase.ecommerce.identity.application.command.SubmitSellerApplicationCommand;
import com.learnfirebase.ecommerce.identity.application.dto.SellerApplicationDto;
import com.learnfirebase.ecommerce.identity.application.port.in.ReviewSellerApplicationUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.SellerApplicationQueryUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.SubmitSellerApplicationUseCase;
import com.learnfirebase.ecommerce.identity.application.port.out.SellerApplicationRepository;
import com.learnfirebase.ecommerce.identity.application.port.out.UserRepository;
import com.learnfirebase.ecommerce.identity.domain.exception.IdentityDomainException;
import com.learnfirebase.ecommerce.identity.domain.model.Role;
import com.learnfirebase.ecommerce.identity.domain.model.SellerApplication;
import com.learnfirebase.ecommerce.identity.domain.model.SellerApplicationId;
import com.learnfirebase.ecommerce.identity.domain.model.SellerApplicationStatus;
import com.learnfirebase.ecommerce.identity.domain.model.User;
import com.learnfirebase.ecommerce.identity.domain.model.UserId;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SellerApplicationService implements SubmitSellerApplicationUseCase, SellerApplicationQueryUseCase, ReviewSellerApplicationUseCase {
    private final SellerApplicationRepository sellerApplicationRepository;
    private final UserRepository userRepository;

    @Override
    public SellerApplicationDto execute(SubmitSellerApplicationCommand command) {
        if (command.getUserId() == null || command.getUserId().isBlank()) {
            throw new IdentityDomainException("User id is required");
        }
        if (command.getStoreName() == null || command.getStoreName().isBlank()) {
            throw new IdentityDomainException("Store name is required");
        }
        if (command.getEmail() == null || command.getEmail().isBlank()) {
            throw new IdentityDomainException("Contact email is required");
        }
        if (command.getPhone() == null || command.getPhone().isBlank()) {
            throw new IdentityDomainException("Phone is required");
        }
        if (command.getDescription() == null || command.getDescription().isBlank()) {
            throw new IdentityDomainException("Store description is required");
        }
        if (!command.isAcceptedTerms()) {
            throw new IdentityDomainException("Seller terms must be accepted");
        }
        User user = userRepository.findById(new UserId(command.getUserId()))
            .orElseThrow(() -> new IdentityDomainException("User not found"));

        sellerApplicationRepository.findLatestByUserId(user.getId()).ifPresent(existing -> {
            if (existing.getStatus() == SellerApplicationStatus.PENDING) {
                throw new IdentityDomainException("Seller application already pending review");
            }
            if (existing.getStatus() == SellerApplicationStatus.APPROVED) {
                throw new IdentityDomainException("User is already approved as seller");
            }
        });

        SellerApplication application = SellerApplication.builder()
            .id(new SellerApplicationId(UUID.randomUUID().toString()))
            .userId(user.getId())
            .storeName(command.getStoreName())
            .contactEmail(new Email(command.getEmail()))
            .phone(command.getPhone())
            .category(command.getCategory())
            .description(command.getDescription())
            .avatarUrl(command.getAvatarUrl())
            .coverUrl(command.getCoverUrl())
            .status(SellerApplicationStatus.PENDING)
            .acceptedTerms(command.isAcceptedTerms())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        SellerApplication saved = sellerApplicationRepository.save(application);
        return toDto(saved);
    }

    @Override
    public List<SellerApplicationDto> list(SellerApplicationStatus status) {
        List<SellerApplication> applications = status != null
            ? sellerApplicationRepository.findByStatus(status)
            : sellerApplicationRepository.findAll();
        return applications.stream().map(this::toDto).toList();
    }

    @Override
    public SellerApplicationDto getLatestForUser(String userId) {
        return sellerApplicationRepository.findLatestByUserId(new UserId(userId))
            .map(this::toDto)
            .orElse(null);
    }

    @Override
    public SellerApplicationDto execute(ReviewSellerApplicationCommand command) {
        SellerApplication application = sellerApplicationRepository.findById(new SellerApplicationId(command.getApplicationId()))
            .orElseThrow(() -> new IdentityDomainException("Seller application not found"));

        SellerApplicationStatus targetStatus = command.isApprove() ? SellerApplicationStatus.APPROVED : SellerApplicationStatus.REJECTED;
        if (application.getStatus() == targetStatus) {
            return toDto(application);
        }
        if (application.getStatus() != SellerApplicationStatus.PENDING) {
            throw new IdentityDomainException("Only pending applications can be reviewed");
        }

        SellerApplication updated = application.toBuilder()
            .status(targetStatus)
            .updatedAt(Instant.now())
            .build();

        SellerApplication saved = sellerApplicationRepository.save(updated);

        if (targetStatus == SellerApplicationStatus.APPROVED) {
            elevateUserToSeller(application.getUserId());
        }

        return toDto(saved);
    }

    private void elevateUserToSeller(UserId userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IdentityDomainException("User not found for seller application"));
        Set<Role> roles = user.getRoles() != null && !user.getRoles().isEmpty()
            ? EnumSet.copyOf(user.getRoles())
            : EnumSet.noneOf(Role.class);
        roles.add(Role.SELLER);

        User updatedUser = User.builder()
            .id(user.getId())
            .email(user.getEmail())
            .password(user.getPassword())
            .authProvider(user.getAuthProvider())
            .providerUserId(user.getProviderUserId())
            .roles(roles)
            .permissions(user.getPermissions())
            .displayName(user.getDisplayName())
            .avatarUrl(user.getAvatarUrl())
            .createdAt(user.getCreatedAt())
            .updatedAt(Instant.now())
            .build();
        userRepository.save(updatedUser);
    }

    private SellerApplicationDto toDto(SellerApplication application) {
        return SellerApplicationDto.builder()
            .id(application.getId().getValue())
            .userId(application.getUserId().getValue())
            .storeName(application.getStoreName())
            .contactEmail(application.getContactEmail() != null ? application.getContactEmail().getValue() : null)
            .phone(application.getPhone())
            .category(application.getCategory())
            .description(application.getDescription())
            .avatarUrl(application.getAvatarUrl())
            .coverUrl(application.getCoverUrl())
            .status(application.getStatus())
            .acceptedTerms(application.isAcceptedTerms())
            .createdAt(application.getCreatedAt())
            .updatedAt(application.getUpdatedAt())
            .build();
    }
}
