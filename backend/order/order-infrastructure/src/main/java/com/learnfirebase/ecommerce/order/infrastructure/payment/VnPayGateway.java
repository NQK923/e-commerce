package com.learnfirebase.ecommerce.order.infrastructure.payment;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnfirebase.ecommerce.order.application.port.out.PaymentGatewayPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VnPayGateway implements PaymentGatewayPort {

    private static final DateTimeFormatter VNP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String SUCCESS_CODE = "00";

    private final VnPayProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PaymentSession initiatePayment(PaymentRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", properties.getTmnCode());
        params.put("vnp_Amount", toVnPayAmount(request.getAmount()));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", request.getOrderId());
        params.put("vnp_OrderInfo", request.getDescription());
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", request.getReturnUrl());
        params.put("vnp_IpAddr", request.getClientIp() == null ? "0.0.0.0" : request.getClientIp());
        params.put("vnp_CreateDate", now.format(VNP_DATE_FORMAT));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(VNP_DATE_FORMAT));

        String query = buildQuery(params);
        String hash = hmacSHA512(properties.getHashSecret(), query);
        String paymentUrl = properties.getPayUrl() + "?" + query + "&vnp_SecureHash=" + hash;

        return PaymentSession.builder()
            .paymentUrl(paymentUrl)
            .reference(request.getOrderId())
            .build();
    }

    @Override
    public PaymentVerification verify(PaymentCallback payload) {
        Map<String, String> params = new TreeMap<>(payload.getParameters());
        String secureHash = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        String toHash = buildQuery(params);
        String expectedHash = hmacSHA512(properties.getHashSecret(), toHash);
        if (secureHash == null || !secureHash.equalsIgnoreCase(expectedHash)) {
            return PaymentVerification.builder()
                .success(false)
                .reference(params.get("vnp_TxnRef"))
                .orderId(params.get("vnp_TxnRef"))
                .rawPayload(toJson(params))
                .errorMessage("Invalid signature")
                .build();
        }

        boolean success = SUCCESS_CODE.equals(params.get("vnp_ResponseCode"));
        BigDecimal amount = parseAmount(params.get("vnp_Amount"));
        String orderId = params.get("vnp_TxnRef");

        return PaymentVerification.builder()
            .success(success)
            .orderId(orderId)
            .reference(orderId)
            .transactionNo(params.getOrDefault("vnp_TransactionNo", params.get("vnp_BankTranNo")))
            .amount(amount)
            .rawPayload(toJson(params))
            .errorMessage(success ? null : "Gateway response code: " + params.get("vnp_ResponseCode"))
            .build();
    }

    private String buildQuery(Map<String, String> params) {
        return params.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + urlEncode(entry.getValue()))
            .collect(Collectors.joining("&"));
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute VNPAY signature", e);
        }
    }

    private String toVnPayAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private BigDecimal parseAmount(String raw) {
        try {
            return new BigDecimal(raw).divide(BigDecimal.valueOf(100));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String toJson(Map<String, String> params) {
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
