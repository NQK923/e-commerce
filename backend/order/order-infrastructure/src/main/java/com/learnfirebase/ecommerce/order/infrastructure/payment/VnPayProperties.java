package com.learnfirebase.ecommerce.order.infrastructure.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "vnpay")
public class VnPayProperties {
    /**
     * Terminal code from VNPAY.
     */
    private String tmnCode;
    /**
     * Secret key used to sign requests.
     */
    private String hashSecret;
    /**
     * Payment URL (sandbox by default).
     */
    private String payUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    /**
     * Return URL where VNPAY redirects after payment.
     */
    private String returnUrl;
}
