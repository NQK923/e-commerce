package com.learnfirebase.ecommerce.identity.adapter.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.identity.application.command.ReviewSellerApplicationCommand;
import com.learnfirebase.ecommerce.identity.application.command.SubmitSellerApplicationCommand;
import com.learnfirebase.ecommerce.identity.application.dto.SellerApplicationDto;
import com.learnfirebase.ecommerce.identity.application.port.in.ReviewSellerApplicationUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.SellerApplicationQueryUseCase;
import com.learnfirebase.ecommerce.identity.application.port.in.SubmitSellerApplicationUseCase;
import com.learnfirebase.ecommerce.identity.domain.exception.IdentityDomainException;
import com.learnfirebase.ecommerce.identity.domain.model.SellerApplicationStatus;
import com.learnfirebase.ecommerce.identity.application.port.in.UserQueryUseCase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@RestController
@RequestMapping("/api/seller/applications")
public class SellerApplicationController {
    private final SubmitSellerApplicationUseCase submitSellerApplicationUseCase;
    private final SellerApplicationQueryUseCase sellerApplicationQueryUseCase;
    private final ReviewSellerApplicationUseCase reviewSellerApplicationUseCase;

    private final UserQueryUseCase userQueryUseCase;

    public SellerApplicationController(
        SubmitSellerApplicationUseCase submitSellerApplicationUseCase,
        SellerApplicationQueryUseCase sellerApplicationQueryUseCase,
        ReviewSellerApplicationUseCase reviewSellerApplicationUseCase,
        UserQueryUseCase userQueryUseCase) {
        this.submitSellerApplicationUseCase = submitSellerApplicationUseCase;
        this.sellerApplicationQueryUseCase = sellerApplicationQueryUseCase;
        this.reviewSellerApplicationUseCase = reviewSellerApplicationUseCase;
        this.userQueryUseCase = userQueryUseCase;
    }

    @PostMapping
    public ResponseEntity<?> submit(
        org.springframework.security.core.Authentication authentication,
        @RequestBody SubmitSellerApplicationRequest request) {
        
        String userId = request.getUserId();
        String email = request.getEmail();

        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }

        String authUserId = authentication.getPrincipal().toString();
        if (userId != null && !userId.equals(authUserId)) {
            return ResponseEntity.status(403).body(ErrorResponse.builder().message("Cannot submit application for another user").build());
        }
        userId = authUserId;

        if (email == null) {
            try {
                email = userQueryUseCase.getById(authUserId).getEmail();
            } catch (Exception ignored) {
            }
        }

        if (email == null) {
            return ResponseEntity.badRequest().body(ErrorResponse.builder().message("Email is required").build());
        }

        try {
            SellerApplicationDto dto = submitSellerApplicationUseCase.execute(SubmitSellerApplicationCommand.builder()
                .userId(userId)
                .storeName(request.getStoreName())
                .email(email)
                .phone(request.getPhone())
                .category(request.getCategory())
                .description(request.getDescription())
                .avatarUrl(request.getAvatarUrl())
                .coverUrl(request.getCoverUrl())
                .acceptedTerms(Boolean.TRUE.equals(request.getAcceptedTerms()))
                .build());
            return ResponseEntity.ok(dto);
        } catch (IdentityDomainException ex) {
            return ResponseEntity.badRequest().body(ErrorResponse.builder().message(ex.getMessage()).build());
        }
    }

    @GetMapping
    public ResponseEntity<List<SellerApplicationDto>> list(@RequestParam(name = "status", required = false) String status) {
        SellerApplicationStatus parsedStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                parsedStatus = SellerApplicationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return ResponseEntity.ok(sellerApplicationQueryUseCase.list(parsedStatus));
    }

    @GetMapping("/me")
    public ResponseEntity<?> myApplication(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        String userId = authentication.getPrincipal().toString();
        
        SellerApplicationDto dto = sellerApplicationQueryUseCase.getLatestForUser(userId);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable("id") String id) {
        return review(id, true);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable("id") String id) {
        return review(id, false);
    }

    private ResponseEntity<?> review(String id, boolean approve) {
        try {
            SellerApplicationDto dto = reviewSellerApplicationUseCase.execute(ReviewSellerApplicationCommand.builder()
                .applicationId(id)
                .approve(approve)
                .build());
            return ResponseEntity.ok(dto);
        } catch (IdentityDomainException ex) {
            return ResponseEntity.badRequest().body(ErrorResponse.builder().message(ex.getMessage()).build());
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class SubmitSellerApplicationRequest {
        private String userId;
        private String storeName;
        private String email;
        private String phone;
        private String category;
        private String description;
        private Boolean acceptedTerms;
        private String avatarUrl;
        private String coverUrl;
    }

    @Value
    @Builder
    static class ErrorResponse {
        String message;
    }
}
