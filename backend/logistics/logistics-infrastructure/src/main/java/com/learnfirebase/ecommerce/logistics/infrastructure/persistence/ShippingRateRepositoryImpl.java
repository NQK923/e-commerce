package com.learnfirebase.ecommerce.logistics.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.learnfirebase.ecommerce.common.domain.valueobject.Money;
import com.learnfirebase.ecommerce.logistics.application.port.out.ShippingRateRepository;
import com.learnfirebase.ecommerce.logistics.domain.model.ShippingRate;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ShippingRateRepositoryImpl implements ShippingRateRepository {
    private final ShippingRateJpaRepository shippingRateJpaRepository;

    @Override
    public Optional<ShippingRate> findByMethodAndDestination(String methodId, String destination) {
        return shippingRateJpaRepository.findByMethodIdAndDestination(methodId, destination)
            .map(entity -> ShippingRate.builder()
                .methodId(entity.getMethodId())
                .destination(entity.getDestination())
                .cost(Money.builder().amount(new BigDecimal(entity.getCost())).currency(entity.getCurrency()).build())
                .build());
    }
}
