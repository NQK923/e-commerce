package com.learnfirebase.ecommerce.order.adapter.web;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.order.application.command.HandlePaymentCallbackCommand;
import com.learnfirebase.ecommerce.order.application.command.InitiatePaymentCommand;
import com.learnfirebase.ecommerce.order.application.dto.OrderDto;
import com.learnfirebase.ecommerce.order.application.dto.PaymentInitResponse;
import com.learnfirebase.ecommerce.order.application.port.in.HandlePaymentCallbackUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.InitiatePaymentUseCase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final InitiatePaymentUseCase initiatePaymentUseCase;
    private final HandlePaymentCallbackUseCase handlePaymentCallbackUseCase;

    @PostMapping("/orders/{orderId}/payment/vnpay")
    public ResponseEntity<PaymentInitResponse> initiateVnPay(
        @PathVariable("orderId") String orderId,
        @RequestBody InitiatePaymentRequest request,
        jakarta.servlet.http.HttpServletRequest servletRequest
    ) {
        String clientIp = resolveClientIp(servletRequest);
        PaymentInitResponse response = initiatePaymentUseCase.initiate(
            InitiatePaymentCommand.builder()
                .orderId(orderId)
                .returnUrl(request.getReturnUrl())
                .clientIp(clientIp)
                .build()
        );
        return ResponseEntity.ok(response);
    }

    private String resolveClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // In case of multiple IPs in X-Forwarded-For, take the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @GetMapping("/payments/vnpay/return")
    public ResponseEntity<OrderDto> handleReturn(@RequestParam Map<String, String> params) {
        OrderDto order = handlePaymentCallbackUseCase.handleCallback(
            HandlePaymentCallbackCommand.builder()
                .parameters(params)
                .build()
        );
        return ResponseEntity.ok(order);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitiatePaymentRequest {
        private String returnUrl;
        // clientIp is now resolved from the request headers
    }
}
