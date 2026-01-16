package com.learnfirebase.ecommerce.order.infrastructure.payment;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.order.application.model.PaymentRecord;
import com.learnfirebase.ecommerce.order.application.model.PaymentStatus;
import com.learnfirebase.ecommerce.order.application.port.out.PaymentTransactionPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentTransactionAdapter implements PaymentTransactionPort {

    private final PaymentTransactionJpaRepository repository;

    @Override
    public void save(PaymentRecord record) {
        PaymentTransactionEntity entity = toEntity(record);
        repository.save(Objects.requireNonNull(entity));
    }

    @Override
    public Optional<PaymentRecord> findByReference(String reference) {
        return repository.findById(Objects.requireNonNull(reference)).map(this::toRecord);
    }

    @Override
    public void updateStatus(String reference, PaymentStatus status, String transactionNo, String rawPayload) {
        repository.findById(Objects.requireNonNull(reference)).ifPresent(entity -> {
            entity.setStatus(mapStatus(status));
            entity.setTransactionNo(transactionNo);
            entity.setRawPayload(rawPayload);
            entity.setUpdatedAt(Instant.now());
            repository.save(entity);
        });
    }

    private PaymentTransactionEntity toEntity(PaymentRecord record) {
        return PaymentTransactionEntity.builder()
                .reference(record.getReference())
                .orderId(record.getOrderId())
                .gateway(record.getGateway())
                .amount(record.getAmount())
                .currency(record.getCurrency())
                .status(mapStatus(record.getStatus()))
                .transactionNo(record.getTransactionNo())
                .rawPayload(record.getRawPayload())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    private PaymentRecord toRecord(PaymentTransactionEntity entity) {
        return PaymentRecord.builder()
                .reference(entity.getReference())
                .orderId(entity.getOrderId())
                .gateway(entity.getGateway())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .status(mapStatus(entity.getStatus()))
                .transactionNo(entity.getTransactionNo())
                .rawPayload(entity.getRawPayload())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private PaymentTransactionStatus mapStatus(PaymentStatus status) {
        return PaymentTransactionStatus.valueOf(status.name());
    }

    private PaymentStatus mapStatus(PaymentTransactionStatus status) {
        return PaymentStatus.valueOf(status.name());
    }
}
