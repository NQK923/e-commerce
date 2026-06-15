package com.learnfirebase.ecommerce.order.infrastructure.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

    @Test
    void initiatePaymentCanReturnSignedDevSuccessCallbackUrl() {
        VnPayProperties devProperties = new VnPayProperties();
        devProperties.setTmnCode("TESTTMN");
        devProperties.setHashSecret(HASH_SECRET);
        devProperties.setPayUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        devProperties.setDevReturnEnabled(true);
        VnPayGateway devGateway = new VnPayGateway(devProperties);

        PaymentGatewayPort.PaymentSession session = devGateway.initiatePayment(
            PaymentGatewayPort.PaymentRequest.builder()
                .orderId("order-1")
                .amount(new BigDecimal("100000"))
                .description("Payment for order order-1")
                .returnUrl("http://localhost:3000/payment/vnpay-return")
                .clientIp("127.0.0.1")
                .build()
        );

        assertThat(session.getReference()).isEqualTo("order-1");
        assertThat(session.getPaymentUrl()).startsWith("http://localhost:3000/payment/vnpay-return?");
        Map<String, String> callbackParams = parseQuery(session.getPaymentUrl());
        assertThat(callbackParams)
            .containsEntry("vnp_TxnRef", "order-1")
            .containsEntry("vnp_ResponseCode", "00")
            .containsEntry("vnp_Amount", "10000000");

        PaymentGatewayPort.PaymentVerification verification = devGateway.verify(
            PaymentGatewayPort.PaymentCallback.builder().parameters(callbackParams).build()
        );

        assertThat(verification.isSuccess()).isTrue();
        assertThat(verification.getReference()).isEqualTo("order-1");
        assertThat(verification.getAmount()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(verification.getTransactionNo()).isEqualTo("DEV-order-1");
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

    private Map<String, String> parseQuery(String url) {
        String query = url.substring(url.indexOf('?') + 1);
        return Arrays.stream(query.split("&"))
            .map(part -> part.split("=", 2))
            .collect(Collectors.toMap(
                pair -> URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                pair -> pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "",
                (first, second) -> first,
                TreeMap::new
            ));
    }
}
