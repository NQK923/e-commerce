package com.learnfirebase.ecommerce.identity.application.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewSellerApplicationCommand {
    private String applicationId;
    private boolean approve;
}
