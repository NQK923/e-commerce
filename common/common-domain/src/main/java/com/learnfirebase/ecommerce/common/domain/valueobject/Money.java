package com.learnfirebase.ecommerce.common.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Money {
    BigDecimal amount;
    String currency;

    public Money add(Money other) {
        requireSameCurrency(other);
        return Money.builder()
            .amount(amount.add(other.amount))
            .currency(currency)
            .build();
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return Money.builder()
            .amount(amount.subtract(other.amount))
            .currency(currency)
            .build();
    }

    public Money multiply(int factor) {
        return Money.builder()
            .amount(amount.multiply(BigDecimal.valueOf(factor)).setScale(2, RoundingMode.HALF_UP))
            .currency(currency)
            .build();
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equalsIgnoreCase(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + currency + " vs " + other.currency);
        }
    }
}
