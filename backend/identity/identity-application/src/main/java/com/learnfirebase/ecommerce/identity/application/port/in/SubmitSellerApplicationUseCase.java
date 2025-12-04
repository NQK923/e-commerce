package com.learnfirebase.ecommerce.identity.application.port.in;

import com.learnfirebase.ecommerce.identity.application.command.SubmitSellerApplicationCommand;
import com.learnfirebase.ecommerce.identity.application.dto.SellerApplicationDto;

public interface SubmitSellerApplicationUseCase {
    SellerApplicationDto execute(SubmitSellerApplicationCommand command);
}
