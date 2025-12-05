package com.learnfirebase.ecommerce.identity.adapter.web;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

    public SellerApplicationController(
        SubmitSellerApplicationUseCase submitSellerApplicationUseCase,
        SellerApplicationQueryUseCase sellerApplicationQueryUseCase,
        ReviewSellerApplicationUseCase reviewSellerApplicationUseCase) {
        this.submitSellerApplicationUseCase = submitSellerApplicationUseCase;
        this.sellerApplicationQueryUseCase = sellerApplicationQueryUseCase;
        this.reviewSellerApplicationUseCase = reviewSellerApplicationUseCase;
    }

    @PostMapping
    public ResponseEntity<?> submit(
        @RequestHeader(name = "Authorization", required = false) String authorization,
        @RequestBody SubmitSellerApplicationRequest request) {
        String userId = request.getUserId() != null ? request.getUserId() : extractUserIdFromAccessToken(authorization);
        String email = request.getEmail() != null ? request.getEmail() : extractEmailFromAccessToken(authorization);
        if (userId == null || email == null) {
            return ResponseEntity.status(401).build();
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
                parsedStatus = null;
            }
        }
        return ResponseEntity.ok(sellerApplicationQueryUseCase.list(parsedStatus));
    }

    @GetMapping("/me")
    public ResponseEntity<?> myApplication(@RequestHeader(name = "Authorization", required = false) String authorization) {
        String userId = extractUserIdFromAccessToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        SellerApplicationDto dto = sellerApplicationQueryUseCase.getLatestForUser(userId);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable String id) {
        return review(id, true);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable String id) {
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

    private String extractUserIdFromAccessToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authorization.substring(7);
            String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length >= 3 && "access".equals(parts[2])) {
                return parts[0];
            }
        } catch (IllegalArgumentException ignored) {
        }
        return null;
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
        }
        return null;
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
