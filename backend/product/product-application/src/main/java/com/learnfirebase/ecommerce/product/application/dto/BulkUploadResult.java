package com.learnfirebase.ecommerce.product.application.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BulkUploadResult {
    boolean success;
    String message;
    int totalRows;
    int successCount;
    int failureCount;
    List<String> errors;
}
