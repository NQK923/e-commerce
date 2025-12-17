package com.learnfirebase.ecommerce.order.application.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentInitResponse {
    String paymentUrl;
    String reference;
}
