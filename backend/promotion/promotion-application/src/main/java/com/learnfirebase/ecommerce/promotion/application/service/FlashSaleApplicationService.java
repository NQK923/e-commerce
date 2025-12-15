package com.learnfirebase.ecommerce.promotion.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.learnfirebase.ecommerce.common.domain.valueobject.Money;
import com.learnfirebase.ecommerce.promotion.application.command.CreateFlashSaleCommand;
import com.learnfirebase.ecommerce.promotion.application.port.in.CreateFlashSaleUseCase;
import com.learnfirebase.ecommerce.promotion.application.port.in.ListFlashSalesUseCase;
import com.learnfirebase.ecommerce.promotion.application.port.out.FlashSaleCachePort;
import com.learnfirebase.ecommerce.promotion.application.port.out.FlashSaleRepository;
import com.learnfirebase.ecommerce.promotion.domain.model.FlashSale;
import com.learnfirebase.ecommerce.promotion.domain.model.FlashSaleId;
import com.learnfirebase.ecommerce.promotion.domain.model.FlashSaleStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FlashSaleApplicationService implements CreateFlashSaleUseCase, ListFlashSalesUseCase {

    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleCachePort flashSaleCachePort;

    @Override
    public FlashSaleId createFlashSale(CreateFlashSaleCommand command) {
        FlashSale flashSale = FlashSale.builder()
            .id(new FlashSaleId(UUID.randomUUID()))
            .productId(command.getProductId())
            .price(Money.builder()
                .amount(command.getPrice())
                .currency(command.getCurrency())
                .build())
            .originalPrice(Money.builder()
                .amount(command.getOriginalPrice())
                .currency(command.getOriginalCurrency())
                .build())
            .startTime(command.getStartTime())
            .endTime(command.getEndTime())
            .totalQuantity(command.getTotalQuantity())
            .remainingQuantity(command.getTotalQuantity())
            .status(FlashSaleStatus.ACTIVE)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        FlashSale saved = flashSaleRepository.save(flashSale);
        
        if (saved.isActive()) {
            flashSaleCachePort.setStock(saved.getId(), saved.getRemainingQuantity());
        }

        return saved.getId();
    }

    @Override
    public List<FlashSale> listActiveFlashSales() {
        return flashSaleRepository.findAllActive();
    }

    @Override
    public List<FlashSale> listAllFlashSales() {
        return flashSaleRepository.findAll();
    }
}
