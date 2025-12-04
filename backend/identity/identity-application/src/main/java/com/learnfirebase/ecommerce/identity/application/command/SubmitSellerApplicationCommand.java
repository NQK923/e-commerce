package com.learnfirebase.ecommerce.identity.application.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmitSellerApplicationCommand {
    private String userId;
    private String storeName;
    private String email;
    private String phone;
    private String category;
    private String description;
    private boolean acceptedTerms;
}
