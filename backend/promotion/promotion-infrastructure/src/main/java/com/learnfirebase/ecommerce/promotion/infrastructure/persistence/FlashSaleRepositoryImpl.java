package com.learnfirebase.ecommerce.promotion.infrastructure.persistence;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.common.domain.valueobject.Money;
import com.learnfirebase.ecommerce.promotion.application.port.out.FlashSaleRepository;
import com.learnfirebase.ecommerce.promotion.domain.model.FlashSale;
import com.learnfirebase.ecommerce.promotion.domain.model.FlashSaleId;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FlashSaleRepositoryImpl implements FlashSaleRepository {

    private final SpringDataFlashSaleRepository repository;

    @Override
    public FlashSale save(FlashSale flashSale) {
        return toDomain(repository.save(Objects.requireNonNull(toEntity(flashSale))));
    }

    @Override
    public Optional<FlashSale> findById(FlashSaleId id) {
        return repository
                .findById(Objects.requireNonNull(java.util.UUID.fromString(Objects.requireNonNull(id.getValue()))))
                .map(this::toDomain);
    }

    @Override
    public Optional<FlashSale> findActiveByProductId(String productId) {
        return repository.findActiveByProductId(Objects.requireNonNull(productId)).map(this::toDomain);
    }

    @Override
    public java.util.List<FlashSale> findAllActive() {
        return repository.findAllActive().stream().map(this::toDomain).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public java.util.List<FlashSale> findAll() {
        return repository.findAll().stream().map(this::toDomain).collect(java.util.stream.Collectors.toList());
    }

    private FlashSaleEntity toEntity(FlashSale flashSale) {
        return FlashSaleEntity.builder()
                .id(flashSale.getId() != null ? java.util.UUID.fromString(flashSale.getId().getValue()) : null)
                .productId(flashSale.getProductId())
                .price(flashSale.getPrice().getAmount())
                .currency(flashSale.getPrice().getCurrency())
                .originalPrice(flashSale.getOriginalPrice().getAmount())
                .originalCurrency(flashSale.getOriginalPrice().getCurrency())
                .startTime(flashSale.getStartTime())
                .endTime(flashSale.getEndTime())
                .totalQuantity(flashSale.getTotalQuantity())
                .remainingQuantity(flashSale.getRemainingQuantity())
                .status(flashSale.getStatus())
                .createdAt(flashSale.getCreatedAt())
                .updatedAt(flashSale.getUpdatedAt())
                .build();
    }

    private FlashSale toDomain(FlashSaleEntity entity) {
        return FlashSale.builder()
                .id(new FlashSaleId(entity.getId()))
                .productId(entity.getProductId())
                .price(Money.builder()
                        .amount(entity.getPrice())
                        .currency(entity.getCurrency())
                        .build())
                .originalPrice(Money.builder()
                        .amount(entity.getOriginalPrice())
                        .currency(entity.getOriginalCurrency())
                        .build())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .totalQuantity(entity.getTotalQuantity())
                .remainingQuantity(entity.getRemainingQuantity())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
