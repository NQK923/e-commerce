package com.learnfirebase.ecommerce.identity.application.port.in;

import java.util.List;

import com.learnfirebase.ecommerce.identity.application.dto.SellerApplicationDto;
import com.learnfirebase.ecommerce.identity.domain.model.SellerApplicationStatus;

public interface SellerApplicationQueryUseCase {
    List<SellerApplicationDto> list(SellerApplicationStatus status);
}
