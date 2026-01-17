package com.learnfirebase.ecommerce.product.application.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.learnfirebase.ecommerce.product.application.command.UpsertProductCommand;
import com.learnfirebase.ecommerce.product.application.dto.BulkUploadResult;
import com.learnfirebase.ecommerce.product.application.port.in.BulkProductUploadUseCase;
import com.learnfirebase.ecommerce.product.application.port.in.ManageProductUseCase;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BulkProductService implements BulkProductUploadUseCase {

    private final ManageProductUseCase manageProductUseCase;

    @Override
    public BulkUploadResult uploadBulk(String sellerId, InputStream csvStream) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        int totalRows = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvStream, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                totalRows++;
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    // Basic CSV parsing (not robust for commas in values)
                    // Format: name,description,price,currency,quantity,categoryId
                    String[] parts = line.split(",");
                    if (parts.length < 6) {
                        throw new IllegalArgumentException("Invalid column count. Expected at least 6.");
                    }

                    String name = parts[0].trim();
                    String description = parts[1].trim();
                    String price = parts[2].trim();
                    String currency = parts[3].trim();
                    int quantity = Integer.parseInt(parts[4].trim());
                    String categoryId = parts[5].trim();

                    UpsertProductCommand command = UpsertProductCommand.builder()
                            .sellerId(sellerId)
                            .name(name)
                            .description(description)
                            .price(price)
                            .currency(currency)
                            .quantity(quantity)
                            .categoryId(categoryId)
                            // Variants and images could be supported with more complex parsing
                            .build();

                    manageProductUseCase.execute(command);
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                    errors.add("Row " + totalRows + ": " + e.getMessage());
                    System.err.println("Failed to process row " + totalRows + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to read CSV stream: " + e.getMessage());
            return BulkUploadResult.builder()
                    .success(false)
                    .message("Failed to process file: " + e.getMessage())
                    .build();
        }

        return BulkUploadResult.builder()
                .success(failureCount == 0)
                .message("Processed " + totalRows + " rows")
                .totalRows(totalRows)
                .successCount(successCount)
                .failureCount(failureCount)
                .errors(errors)
                .build();
    }
}
