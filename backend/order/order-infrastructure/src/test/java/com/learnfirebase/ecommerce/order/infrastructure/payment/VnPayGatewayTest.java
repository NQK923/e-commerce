package com.learnfirebase.ecommerce.order.infrastructure.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.learnfirebase.ecommerce.order.application.port.out.PaymentGatewayPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VnPayGatewayTest {
    private static final String HASH_SECRET = "test-secret";

    private VnPayGateway gateway;

    @BeforeEach
    void setUp() {
        VnPayProperties properties = new VnPayProperties();
        properties.setTmnCode("TESTTMN");
        properties.setHashSecret(HASH_SECRET);
        properties.setPayUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        gateway = new VnPayGateway(properties);
    }

    @Test
    void verifyAcceptsSignedSuccessCallbackAndParsesAmount() {
        Map<String, String> params = signedParams(Map.of(
            "vnp_TxnRef", "order-1",
            "vnp_ResponseCode", "00",
            "vnp_TransactionNo", "txn-1",
            "vnp_Amount", "10000000",
            "vnp_OrderInfo", "Payment for order order-1"
        ));

        PaymentGatewayPort.PaymentVerification verification = gateway.verify(
            PaymentGatewayPort.PaymentCallback.builder().parameters(params).build()
        );

        assertThat(verification.isSuccess()).isTrue();
        assertThat(verification.getOrderId()).isEqualTo("order-1");
        assertThat(verification.getReference()).isEqualTo("order-1");
        assertThat(verification.getTransactionNo()).isEqualTo("txn-1");
        assertThat(verification.getAmount()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(verification.getErrorMessage()).isNull();
        assertThat(verification.getRawPayload()).contains("\"vnp_TxnRef\":\"order-1\"");
    }

    @Test
    void verifyRejectsInvalidSignature() {
        Map<String, String> params = signedParams(Map.of(
            "vnp_TxnRef", "order-1",
            "vnp_ResponseCode", "00",
            "vnp_TransactionNo", "txn-1",
            "vnp_Amount", "10000000"
        ));
        params.put("vnp_Amount", "1");

        PaymentGatewayPort.PaymentVerification verification = gateway.verify(
            PaymentGatewayPort.PaymentCallback.builder().parameters(params).build()
        );

        assertThat(verification.isSuccess()).isFalse();
        assertThat(verification.getOrderId()).isEqualTo("order-1");
        assertThat(verification.getReference()).isEqualTo("order-1");
        assertThat(verification.getErrorMessage()).isEqualTo("Invalid signature");
    }

    @Test
    void verifyReturnsFailedStatusForSignedGatewayFailureCode() {
        Map<String, String> params = signedParams(Map.of(
            "vnp_TxnRef", "order-1",
            "vnp_ResponseCode", "24",
            "vnp_BankTranNo", "bank-txn-1",
            "vnp_Amount", "10000000"
        ));

        PaymentGatewayPort.PaymentVerification verification = gateway.verify(
            PaymentGatewayPort.PaymentCallback.builder().parameters(params).build()
        );

        assertThat(verification.isSuccess()).isFalse();
        assertThat(verification.getTransactionNo()).isEqualTo("bank-txn-1");
        assertThat(verification.getAmount()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(verification.getErrorMessage()).isEqualTo("Gateway response code: 24");
    }

    private Map<String, String> signedParams(Map<String, String> params) {
        Map<String, String> sorted = new TreeMap<>(params);
        String signature = hmacSHA512(HASH_SECRET, buildQuery(sorted));
        sorted.put("vnp_SecureHash", signature);
        return sorted;
    }

    private String buildQuery(Map<String, String> params) {
        return params.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte hashByte : hashBytes) {
                result.append(String.format("%02x", hashByte));
            }
            return result.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign test payload", e);
        }
    }
}
