package com.learnfirebase.ecommerce.promotion.application.command;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateFlashSaleCommand {
    private String productId;
    private BigDecimal price;
    private String currency;
    private BigDecimal originalPrice;
    private String originalCurrency;
    private Instant startTime;
    private Instant endTime;
    private Integer totalQuantity;
}
