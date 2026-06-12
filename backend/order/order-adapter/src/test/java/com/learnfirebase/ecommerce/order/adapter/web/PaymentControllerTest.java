package com.learnfirebase.ecommerce.order.adapter.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import com.learnfirebase.ecommerce.order.application.command.InitiatePaymentCommand;
import com.learnfirebase.ecommerce.order.application.dto.OrderDto;
import com.learnfirebase.ecommerce.order.application.dto.PaymentInitResponse;
import com.learnfirebase.ecommerce.order.application.port.in.GetOrderUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.HandlePaymentCallbackUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.InitiatePaymentUseCase;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {
    @Mock
    private InitiatePaymentUseCase initiatePaymentUseCase;

    @Mock
    private HandlePaymentCallbackUseCase handlePaymentCallbackUseCase;

    @Mock
    private GetOrderUseCase getOrderUseCase;

    @Test
    void initiateVnPayAllowsBuyerOwner() {
        PaymentController controller = controller();
        when(getOrderUseCase.getOrder("order-1")).thenReturn(order("buyer-1"));
        when(initiatePaymentUseCase.initiate(any())).thenReturn(PaymentInitResponse.builder()
            .paymentUrl("https://sandbox.local/pay")
            .reference("ref-1")
            .build());

        var request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        var response = controller.initiateVnPay(
            "order-1",
            new PaymentController.InitiatePaymentRequest("http://localhost:3000/payment/vnpay-return"),
            request,
            auth("buyer-1", "ROLE_CUSTOMER"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ArgumentCaptor<InitiatePaymentCommand> command = ArgumentCaptor.forClass(InitiatePaymentCommand.class);
        verify(initiatePaymentUseCase).initiate(command.capture());
        assertEquals("order-1", command.getValue().getOrderId());
        assertEquals("127.0.0.1", command.getValue().getClientIp());
    }

    @Test
    void initiateVnPayRejectsNonOwnerSeller() {
        PaymentController controller = controller();
        when(getOrderUseCase.getOrder("order-1")).thenReturn(order("buyer-1"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            controller.initiateVnPay(
                "order-1",
                new PaymentController.InitiatePaymentRequest("http://localhost:3000/payment/vnpay-return"),
                new MockHttpServletRequest(),
                auth("seller-1", "ROLE_SELLER")));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(initiatePaymentUseCase, never()).initiate(any());
    }

    private PaymentController controller() {
        return new PaymentController(initiatePaymentUseCase, handlePaymentCallbackUseCase, getOrderUseCase);
    }

    private OrderDto order(String userId) {
        return OrderDto.builder()
            .id("order-1")
            .userId(userId)
            .status("PENDING")
            .currency("VND")
            .totalAmount("100000")
            .build();
    }

    private UsernamePasswordAuthenticationToken auth(String userId, String role) {
        return new UsernamePasswordAuthenticationToken(
            userId,
            "token",
            List.of(new SimpleGrantedAuthority(role)));
    }
}
