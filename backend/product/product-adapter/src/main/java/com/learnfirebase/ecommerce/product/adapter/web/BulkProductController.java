package com.learnfirebase.ecommerce.product.adapter.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import java.security.Principal;

/**
 * Controller for bulk product operations
 */
@RestController
@RequestMapping("/api/seller/products")
@RequiredArgsConstructor
public class BulkProductController {

    private final com.learnfirebase.ecommerce.product.application.port.in.BulkProductUploadUseCase bulkProductUploadUseCase;

    @PostMapping("/bulk")
    public ResponseEntity<com.learnfirebase.ecommerce.product.application.dto.BulkUploadResult> uploadBulk(
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        String sellerId = principal.getName();

        try {
            com.learnfirebase.ecommerce.product.application.dto.BulkUploadResult result = bulkProductUploadUseCase
                    .uploadBulk(sellerId, file.getInputStream());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(com.learnfirebase.ecommerce.product.application.dto.BulkUploadResult.builder()
                            .success(false)
                            .message("Upload failed: " + e.getMessage())
                            .build());
        }
    }

}
