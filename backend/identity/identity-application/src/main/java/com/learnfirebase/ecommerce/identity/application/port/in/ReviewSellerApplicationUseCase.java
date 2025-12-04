package com.learnfirebase.ecommerce.identity.application.port.in;

import com.learnfirebase.ecommerce.identity.application.command.ReviewSellerApplicationCommand;
import com.learnfirebase.ecommerce.identity.application.dto.SellerApplicationDto;

public interface ReviewSellerApplicationUseCase {
    SellerApplicationDto execute(ReviewSellerApplicationCommand command);
}
