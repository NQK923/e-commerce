package com.learnfirebase.ecommerce.promotion.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.learnfirebase.ecommerce.common.domain.valueobject.Money;
import com.learnfirebase.ecommerce.promotion.application.command.CreateFlashSaleCommand;
import com.learnfirebase.ecommerce.promotion.application.port.in.CreateFlashSaleUseCase;
import com.learnfirebase.ecommerce.promotion.application.port.in.ListFlashSalesUseCase;
import com.learnfirebase.ecommerce.promotion.application.port.out.FlashSaleCachePort;
import com.learnfirebase.ecommerce.promotion.application.port.out.FlashSaleRepository;
import com.learnfirebase.ecommerce.promotion.domain.exception.PromotionDomainException;
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
        validate(command);

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

    private void validate(CreateFlashSaleCommand command) {
        if (command.getProductId() == null || command.getProductId().isBlank()) {
            throw new PromotionDomainException("Product id is required");
        }
        if (command.getPrice() == null || command.getPrice().signum() <= 0) {
            throw new PromotionDomainException("Flash sale price must be greater than zero");
        }
        if (command.getOriginalPrice() == null || command.getOriginalPrice().signum() <= 0) {
            throw new PromotionDomainException("Original price must be greater than zero");
        }
        if (command.getPrice().compareTo(command.getOriginalPrice()) >= 0) {
            throw new PromotionDomainException("Flash sale price must be lower than original price");
        }
        if (command.getCurrency() == null || command.getCurrency().isBlank()) {
            throw new PromotionDomainException("Currency is required");
        }
        if (command.getOriginalCurrency() == null || command.getOriginalCurrency().isBlank()) {
            throw new PromotionDomainException("Original currency is required");
        }
        if (!command.getCurrency().equalsIgnoreCase(command.getOriginalCurrency())) {
            throw new PromotionDomainException("Flash sale currency must match original currency");
        }
        if (command.getStartTime() == null || command.getEndTime() == null) {
            throw new PromotionDomainException("Flash sale start and end time are required");
        }
        if (!command.getEndTime().isAfter(command.getStartTime())) {
            throw new PromotionDomainException("Flash sale end time must be after start time");
        }
        if (command.getTotalQuantity() == null || command.getTotalQuantity() <= 0) {
            throw new PromotionDomainException("Flash sale quantity must be greater than zero");
        }
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
