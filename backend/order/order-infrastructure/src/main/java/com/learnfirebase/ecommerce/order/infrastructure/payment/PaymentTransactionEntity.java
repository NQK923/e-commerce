package com.learnfirebase.ecommerce.order.infrastructure.payment;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionEntity {
    @Id
    private String reference;
    private String orderId;
    private String gateway;
    private BigDecimal amount;
    private String currency;
    @Enumerated(EnumType.STRING)
    private PaymentTransactionStatus status;
    private String transactionNo;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String rawPayload;
    private Instant createdAt;
    private Instant updatedAt;
}
