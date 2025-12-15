package com.learnfirebase.ecommerce.order.infrastructure.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.order.application.port.out.LoadFlashSalePort;
import com.learnfirebase.ecommerce.promotion.application.port.out.FlashSaleRepository;
import com.learnfirebase.ecommerce.promotion.domain.model.FlashSale;
import com.learnfirebase.ecommerce.promotion.domain.model.FlashSaleId;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoadFlashSaleAdapter implements LoadFlashSalePort {

    private final FlashSaleRepository flashSaleRepository;

    @Override
    public Optional<FlashSale> loadFlashSale(String flashSaleId) {
        return flashSaleRepository.findById(new FlashSaleId(UUID.fromString(flashSaleId)));
    }
}
