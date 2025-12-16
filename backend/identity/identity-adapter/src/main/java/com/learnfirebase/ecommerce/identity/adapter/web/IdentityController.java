package com.learnfirebase.ecommerce.identity.adapter.web;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
        return ResponseEntity.ok(registerUserUseCase.execute(command));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenDto> login(@RequestBody LoginCommand command) {
        return ResponseEntity.ok(authenticateUserUseCase.execute(command));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@RequestHeader(name = "Authorization", required = false) String authorization) {
        String email = extractEmailFromAccessToken(authorization);
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userQueryUseCase.getByEmail(email));
    }

    @GetMapping("/all")
    public ResponseEntity<java.util.List<UserDto>> listUsers() {
        return ResponseEntity.ok(listUsersUseCase.list());
    }

    @PatchMapping("/me")
    public ResponseEntity<UserDto> updateProfile(@RequestHeader(name = "Authorization", required = false) String authorization,
        @RequestBody UpdateProfileRequest request) {
        String email = extractEmailFromAccessToken(authorization);
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        UserDto user = userQueryUseCase.getByEmail(email);
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
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody AddAddressRequest request) {
        String email = extractEmailFromAccessToken(authorization);
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        UserDto user = userQueryUseCase.getByEmail(email);
        
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
                
        return ResponseEntity.ok(manageUserAddressUseCase.addAddress(user.getId(), command));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @PathVariable("addressId") String addressId) {
        String email = extractEmailFromAccessToken(authorization);
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        UserDto user = userQueryUseCase.getByEmail(email);
        
        manageUserAddressUseCase.deleteAddress(user.getId(), addressId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") String id) {
        return ResponseEntity.ok(userQueryUseCase.getById(id));
    }

    private String extractEmailFromAccessToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authorization.substring(7);
            String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length >= 2) {
                return parts[1];
            }
        } catch (IllegalArgumentException ignored) {
            // invalid token
        }
        return null;
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
