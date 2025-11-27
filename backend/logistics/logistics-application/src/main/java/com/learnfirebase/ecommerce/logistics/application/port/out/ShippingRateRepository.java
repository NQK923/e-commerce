package com.learnfirebase.ecommerce.logistics.application.port.out;

import java.util.Optional;

import com.learnfirebase.ecommerce.logistics.domain.model.ShippingRate;

public interface ShippingRateRepository {
    Optional<ShippingRate> findByMethodAndDestination(String methodId, String destination);
}
