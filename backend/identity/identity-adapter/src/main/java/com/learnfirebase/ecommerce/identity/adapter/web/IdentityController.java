package com.learnfirebase.ecommerce.identity.adapter.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.common.domain.valueobject.Address;
import com.learnfirebase.ecommerce.identity.application.command.AddAddressCommand;
import com.learnfirebase.ecommerce.identity.application.command.LoginCommand;
import com.learnfirebase.ecommerce.identity.application.command.RegisterUserCommand;
import com.learnfirebase.ecommerce.identity.application.command.UpdateProfileCommand;
import com.learnfirebase.ecommerce.identity.application.dto.AuthTokenDto;
import com.learnfirebase.ecommerce.identity.application.dto.UserAddressDto;
import com.learnfirebase.ecommerce.identity.application.dto.UserDto;
import com.learnfirebase.ecommerce.identity.application.port.in.AuthenticateUserUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.ManageUserAddressUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.RegisterUserUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.UserQueryUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.UpdateUserProfileUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.ListUsersUseCase;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class IdentityController {
    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final UserQueryUseCase userQueryUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final ManageUserAddressUseCase manageUserAddressUseCase;

    @PostMapping
    public ResponseEntity<UserDto> register(@RequestBody RegisterUserCommand command) {
        try {
            return ResponseEntity.ok(registerUserUseCase.execute(command));
        } catch (com.learnfirebase.ecommerce.identity.domain.exception.IdentityDomainException ex) {
            if ("OTP_REQUIRED".equals(ex.getMessage())) {
                return ResponseEntity.status(428).build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenDto> login(@RequestBody LoginCommand command) {
        try {
            return ResponseEntity.ok(authenticateUserUseCase.execute(command));
        } catch (com.learnfirebase.ecommerce.identity.domain.exception.IdentityDomainException ex) {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        String userId = authentication.getPrincipal().toString();
        return ResponseEntity.ok(userQueryUseCase.getById(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<java.util.List<UserDto>> listUsers() {
        return ResponseEntity.ok(listUsersUseCase.list());
    }

    @PatchMapping("/me")
    public ResponseEntity<UserDto> updateProfile(org.springframework.security.core.Authentication authentication,
        @RequestBody UpdateProfileRequest request) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        String userId = authentication.getPrincipal().toString();
        UserDto user = userQueryUseCase.getById(userId);
        UpdateProfileCommand command = UpdateProfileCommand.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .displayName(request.getDisplayName())
            .avatarUrl(request.getAvatarUrl())
            .build();
        UserDto updated = updateUserProfileUseCase.execute(command);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/me/addresses")
    public ResponseEntity<UserAddressDto> addAddress(
            org.springframework.security.core.Authentication authentication,
            @RequestBody AddAddressRequest request) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        String userId = authentication.getPrincipal().toString();
        
        AddAddressCommand command = AddAddressCommand.builder()
                .label(request.getLabel())
                .isDefault(request.isDefault())
                .address(Address.builder()
                        .fullName(request.getFullName())
                        .phoneNumber(request.getPhoneNumber())
                        .line1(request.getLine1())
                        .line2(request.getLine2())
                        .city(request.getCity())
                        .state(request.getState())
                        .postalCode(request.getPostalCode())
                        .country(request.getCountry())
                        .build())
                .build();
                
        return ResponseEntity.ok(manageUserAddressUseCase.addAddress(userId, command));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            org.springframework.security.core.Authentication authentication,
            @PathVariable("addressId") String addressId) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        String userId = authentication.getPrincipal().toString();
        
        manageUserAddressUseCase.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") String id) {
        return ResponseEntity.ok(userQueryUseCase.getById(id));
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class UpdateProfileRequest {
        private String displayName;
        private String avatarUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class AddAddressRequest {
        private String label;
        private boolean isDefault;
        private String fullName;
        private String phoneNumber;
        private String line1;
        private String line2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }
}
