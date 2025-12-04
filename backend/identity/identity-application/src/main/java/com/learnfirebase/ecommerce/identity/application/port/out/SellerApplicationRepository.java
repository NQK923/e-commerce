package com.learnfirebase.ecommerce.identity.application.port.out;

import java.util.List;
import java.util.Optional;

import com.learnfirebase.ecommerce.identity.domain.model.SellerApplication;
import com.learnfirebase.ecommerce.identity.domain.model.SellerApplicationId;
import com.learnfirebase.ecommerce.identity.domain.model.SellerApplicationStatus;
import com.learnfirebase.ecommerce.identity.domain.model.UserId;

public interface SellerApplicationRepository {
    SellerApplication save(SellerApplication application);
    Optional<SellerApplication> findById(SellerApplicationId id);
    Optional<SellerApplication> findLatestByUserId(UserId userId);
    List<SellerApplication> findAll();
    List<SellerApplication> findByStatus(SellerApplicationStatus status);
}
