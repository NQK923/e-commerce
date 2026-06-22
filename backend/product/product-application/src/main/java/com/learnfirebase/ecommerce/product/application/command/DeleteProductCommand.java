package com.learnfirebase.ecommerce.product.application.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteProductCommand {
    private String id;
    private String sellerId; // Optional: Only sellers should be able to delete their own product, or admins
    private boolean isAdmin;
}
