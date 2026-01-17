package com.learnfirebase.ecommerce.order.application.service;

import lombok.RequiredArgsConstructor;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * VNPay payment gateway service
 * Handles payment URL generation and signature validation
 * Note: Configuration values should be injected via constructor
 */
@RequiredArgsConstructor
public class VNPayService {

    private final String tmnCode;
    private final String hashSecret;
    private final String vnpayUrl;
    private final String version;

    // Constructor for easy instantiation with config
    public VNPayService(String tmnCode, String hashSecret, String vnpayUrl) {
        this(tmnCode, hashSecret, vnpayUrl, "2.1.0");
    }

    private static final String VNP_COMMAND = "pay";
    private static final String VNP_CURRENCY_CODE = "VND";
    private static final String VNP_LOCALE = "vn";

    /**
     * Generate VNPay payment URL
     */
    public String generatePaymentUrl(String orderId, long amount, String orderInfo, String returnUrl, String clientIp) {
        Map<String, String> vnpParams = new TreeMap<>();

        vnpParams.put("vnp_Version", version);
        vnpParams.put("vnp_Command", VNP_COMMAND);
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay uses smallest currency unit
        vnpParams.put("vnp_CurrCode", VNP_CURRENCY_CODE);
        vnpParams.put("vnp_TxnRef", orderId);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", VNP_LOCALE);
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", clientIp);

        // Add timestamp
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnpCreateDate = formatter.format(new Date());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        // Expiration time: 15 minutes from now
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        calendar.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(calendar.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        // Build query string and sign
        String queryString = buildQueryString(vnpParams);
        String secureHash = hmacSHA512(hashSecret, queryString);

        return vnpayUrl + "?" + queryString + "&vnp_SecureHash=" + secureHash;
    }

    /**
     * Validate VNPay return signature
     */
    public boolean validateSignature(Map<String, String> params) {
        String vnpSecureHash = params.get("vnp_SecureHash");
        if (vnpSecureHash == null) {
            return false;
        }

        // Remove signature from params before validation
        Map<String, String> validateParams = new TreeMap<>(params);
        validateParams.remove("vnp_SecureHash");
        validateParams.remove("vnp_SecureHashType");

        String queryString = buildQueryString(validateParams);
        String calculatedHash = hmacSHA512(hashSecret, queryString);

        return vnpSecureHash.equals(calculatedHash);
    }

    /**
     * Build query string from parameters
     */
    private String buildQueryString(Map<String, String> params) {
        StringBuilder query = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                if (query.length() > 0) {
                    query.append("&");
                }
                try {
                    query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()))
                            .append("=")
                            .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("UTF-8 encoding not supported", e);
                }
            }
        }

        return query.toString();
    }

    /**
     * Generate HMAC SHA512 signature
     */
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] hash = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder();
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA512", e);
        }
    }

    /**
     * Get transaction status from response code
     */
    public String getTransactionStatus(String responseCode) {
        return "00".equals(responseCode) ? "SUCCESS" : "FAILED";
    }
}
