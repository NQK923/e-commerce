package com.learnfirebase.ecommerce.order.application.port.out;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

public interface PaymentGatewayPort {

    PaymentSession initiatePayment(PaymentRequest request);

    PaymentVerification verify(PaymentCallback payload);

    @Value
    @Builder
    class PaymentRequest {
        String orderId;
        BigDecimal amount;
        String currency;
        String description;
        String returnUrl;
        String clientIp;
    }

    @Value
    @Builder
    class PaymentSession {
        String paymentUrl;
        String reference;
    }

    @Value
    @Builder
    class PaymentCallback {
        java.util.Map<String, String> parameters;
    }

    @Value
    @Builder
    class PaymentVerification {
        boolean success;
        String orderId;
        String reference;
        String transactionNo;
        BigDecimal amount;
        String rawPayload;
        String errorMessage;
    }
}
