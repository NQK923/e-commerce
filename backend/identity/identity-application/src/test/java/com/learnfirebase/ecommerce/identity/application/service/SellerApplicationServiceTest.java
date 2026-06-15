package com.learnfirebase.ecommerce.identity.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;

import com.learnfirebase.ecommerce.common.domain.valueobject.Email;
import com.learnfirebase.ecommerce.identity.application.command.ReviewSellerApplicationCommand;
import com.learnfirebase.ecommerce.identity.application.port.out.SellerApplicationRepository;
import com.learnfirebase.ecommerce.identity.application.port.out.UserRepository;
import com.learnfirebase.ecommerce.identity.domain.exception.IdentityDomainException;
import com.learnfirebase.ecommerce.identity.domain.model.AuthProvider;
import com.learnfirebase.ecommerce.identity.domain.model.HashedPassword;
import com.learnfirebase.ecommerce.identity.domain.model.Role;
import com.learnfirebase.ecommerce.identity.domain.model.SellerApplication;
import com.learnfirebase.ecommerce.identity.domain.model.SellerApplicationId;
import com.learnfirebase.ecommerce.identity.domain.model.SellerApplicationStatus;
import com.learnfirebase.ecommerce.identity.domain.model.User;
import com.learnfirebase.ecommerce.identity.domain.model.UserId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SellerApplicationServiceTest {
    @Mock
    private SellerApplicationRepository sellerApplicationRepository;
    @Mock
    private UserRepository userRepository;

    private SellerApplicationService service;

    @BeforeEach
    void setUp() {
        service = new SellerApplicationService(sellerApplicationRepository, userRepository);
    }

    @Test
    void approvingPendingApplicationElevatesUserToSeller() {
        SellerApplication pending = application(SellerApplicationStatus.PENDING);
        User buyer = user(EnumSet.of(Role.CUSTOMER));
        when(sellerApplicationRepository.findById(new SellerApplicationId("application-1")))
            .thenReturn(Optional.of(pending));
        when(sellerApplicationRepository.save(org.mockito.ArgumentMatchers.any(SellerApplication.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(new UserId("user-1"))).thenReturn(Optional.of(buyer));

        var result = service.execute(ReviewSellerApplicationCommand.builder()
            .applicationId("application-1")
            .approve(true)
            .build());

        assertThat(result.getStatus()).isEqualTo(SellerApplicationStatus.APPROVED);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRoles()).contains(Role.CUSTOMER, Role.SELLER);
    }

    @Test
    void rejectingPendingApplicationDoesNotElevateUser() {
        SellerApplication pending = application(SellerApplicationStatus.PENDING);
        when(sellerApplicationRepository.findById(new SellerApplicationId("application-1")))
            .thenReturn(Optional.of(pending));
        when(sellerApplicationRepository.save(org.mockito.ArgumentMatchers.any(SellerApplication.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.execute(ReviewSellerApplicationCommand.builder()
            .applicationId("application-1")
            .approve(false)
            .build());

        assertThat(result.getStatus()).isEqualTo(SellerApplicationStatus.REJECTED);
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void reviewIsIdempotentWhenApplicationAlreadyHasTargetStatus() {
        SellerApplication approved = application(SellerApplicationStatus.APPROVED);
        when(sellerApplicationRepository.findById(new SellerApplicationId("application-1")))
            .thenReturn(Optional.of(approved));

        var result = service.execute(ReviewSellerApplicationCommand.builder()
            .applicationId("application-1")
            .approve(true)
            .build());

        assertThat(result.getStatus()).isEqualTo(SellerApplicationStatus.APPROVED);
        verify(sellerApplicationRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void reviewRejectsNonPendingApplicationWhenTargetDiffers() {
        SellerApplication rejected = application(SellerApplicationStatus.REJECTED);
        when(sellerApplicationRepository.findById(new SellerApplicationId("application-1")))
            .thenReturn(Optional.of(rejected));

        assertThatThrownBy(() -> service.execute(ReviewSellerApplicationCommand.builder()
            .applicationId("application-1")
            .approve(true)
            .build()))
            .isInstanceOf(IdentityDomainException.class)
            .hasMessage("Only pending applications can be reviewed");
    }

    private SellerApplication application(SellerApplicationStatus status) {
        return SellerApplication.builder()
            .id(new SellerApplicationId("application-1"))
            .userId(new UserId("user-1"))
            .storeName("Smoke Store")
            .contactEmail(new Email("seller@example.local"))
            .phone("0900000000")
            .category("electronics")
            .description("Local smoke seller")
            .acceptedTerms(true)
            .status(status)
            .createdAt(Instant.parse("2026-06-15T00:00:00Z"))
            .updatedAt(Instant.parse("2026-06-15T00:00:00Z"))
            .build();
    }

    private User user(EnumSet<Role> roles) {
        return User.builder()
            .id(new UserId("user-1"))
            .email(new Email("seller@example.local"))
            .password(new HashedPassword("hashed"))
            .authProvider(AuthProvider.LOCAL)
            .roles(roles)
            .displayName("Seller")
            .createdAt(Instant.parse("2026-06-15T00:00:00Z"))
            .updatedAt(Instant.parse("2026-06-15T00:00:00Z"))
            .build();
    }
}
