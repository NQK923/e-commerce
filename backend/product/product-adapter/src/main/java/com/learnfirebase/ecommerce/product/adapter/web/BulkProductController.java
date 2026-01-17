package com.learnfirebase.ecommerce.product.adapter.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;

/**
 * Controller for bulk product operations
 */
@RestController
@RequestMapping("/api/seller/products")
@RequiredArgsConstructor
public class BulkProductController {

    // Placeholder for bulk upload service
    // private final BulkProductUploadService bulkProductUploadService;

    @PostMapping("/bulk")
    public ResponseEntity<BulkUploadResponse> uploadBulk(
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        String sellerId = principal.getName();

        // TODO: Implement CSV parsing and bulk product creation
        // For now, return placeholder response
        BulkUploadResponse response = new BulkUploadResponse();
        response.setSuccess(false);
        response.setMessage("Bulk upload feature coming soon!");
        response.setTotalRows(0);
        response.setSuccessCount(0);
        response.setFailureCount(0);

        return ResponseEntity.ok(response);
    }

    @Data
    public static class BulkUploadResponse {
        private boolean success;
        private String message;
        private int totalRows;
        private int successCount;
        private int failureCount;
        private List<String> errors;
    }
}
