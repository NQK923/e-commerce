package com.learnfirebase.ecommerce.order.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.learnfirebase.ecommerce.common.domain.valueobject.Money;
import com.learnfirebase.ecommerce.order.application.command.HandlePaymentCallbackCommand;
import com.learnfirebase.ecommerce.order.application.dto.OrderDto;
import com.learnfirebase.ecommerce.order.application.model.PaymentStatus;
import com.learnfirebase.ecommerce.order.application.port.out.InventoryReservationPort;
import com.learnfirebase.ecommerce.order.application.port.out.LoadFlashSalePort;
import com.learnfirebase.ecommerce.order.application.port.out.LoadProductPort;
import com.learnfirebase.ecommerce.order.application.port.out.OrderOutboxPort;
import com.learnfirebase.ecommerce.order.application.port.out.OrderRepository;
import com.learnfirebase.ecommerce.order.application.port.out.PaymentGatewayPort;
import com.learnfirebase.ecommerce.order.application.port.out.PaymentTransactionPort;
import com.learnfirebase.ecommerce.order.domain.exception.OrderDomainException;
import com.learnfirebase.ecommerce.order.domain.model.Order;
import com.learnfirebase.ecommerce.order.domain.model.OrderId;
import com.learnfirebase.ecommerce.order.domain.model.OrderItem;
import com.learnfirebase.ecommerce.order.domain.model.OrderStatus;
import com.learnfirebase.ecommerce.order.domain.model.UserId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServicePaymentCallbackTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private LoadProductPort loadProductPort;
    @Mock
    private LoadFlashSalePort loadFlashSalePort;
    @Mock
    private InventoryReservationPort inventoryReservationPort;
    @Mock
    private OrderOutboxPort orderOutboxPort;
    @Mock
    private PaymentGatewayPort paymentGatewayPort;
    @Mock
    private PaymentTransactionPort paymentTransactionPort;

    private OrderApplicationService service;

    @BeforeEach
    void setUp() {
        service = new OrderApplicationService(
            orderRepository,
            loadProductPort,
            loadFlashSalePort,
            inventoryReservationPort,
            orderOutboxPort,
            paymentGatewayPort,
            paymentTransactionPort
        );
    }

    @Test
    void successfulCallbackPaysPendingOrderAndConfirmsInventory() {
        Order order = order("order-1", OrderStatus.PENDING, "100000.00");
        when(paymentGatewayPort.verify(any())).thenReturn(successfulVerification("order-1", "100000.00"));
        when(orderRepository.findById(new OrderId("order-1"))).thenReturn(Optional.of(order));
        when(paymentTransactionPort.findByReference("order-1")).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDto result = service.handleCallback(callbackCommand());

        assertThat(result.getStatus()).isEqualTo("PAID");
        verify(paymentTransactionPort).save(any());
        verify(paymentTransactionPort).updateStatus("order-1", PaymentStatus.SUCCESS, "txn-1", "{\"ok\":true}");
        verify(inventoryReservationPort).confirm("order-1");
        verify(orderOutboxPort).saveEvent(any());
    }

    @Test
    void failedGatewayCallbackRecordsFailureAndDoesNotLoadOrPayOrder() {
        when(paymentGatewayPort.verify(any())).thenReturn(PaymentGatewayPort.PaymentVerification.builder()
            .success(false)
            .orderId("order-1")
            .reference("order-1")
            .transactionNo("txn-1")
            .rawPayload("{\"ok\":false}")
            .errorMessage("Invalid signature")
            .build());

        assertThatThrownBy(() -> service.handleCallback(callbackCommand()))
            .isInstanceOf(OrderDomainException.class)
            .hasMessage("Invalid signature");

        verify(paymentTransactionPort).updateStatus("order-1", PaymentStatus.FAILED, "txn-1", "{\"ok\":false}");
        verify(orderRepository, never()).findById(any());
        verify(inventoryReservationPort, never()).confirm(any());
    }

    @Test
    void amountMismatchRecordsFailureAndDoesNotPayOrder() {
        Order order = order("order-1", OrderStatus.PENDING, "100000.00");
        when(paymentGatewayPort.verify(any())).thenReturn(successfulVerification("order-1", "99999.00"));
        when(orderRepository.findById(new OrderId("order-1"))).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.handleCallback(callbackCommand()))
            .isInstanceOf(OrderDomainException.class)
            .hasMessage("Payment amount mismatch");

        verify(paymentTransactionPort).updateStatus("order-1", PaymentStatus.FAILED, "txn-1", "{\"ok\":true}");
        verify(orderRepository, never()).save(any());
        verify(inventoryReservationPort, never()).confirm(any());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void duplicateCallbackForAlreadyPaidOrderIsIdempotent() {
        Order order = order("order-1", OrderStatus.PAID, "100000.00");
        when(paymentGatewayPort.verify(any())).thenReturn(successfulVerification("order-1", "100000.00"));
        when(orderRepository.findById(new OrderId("order-1"))).thenReturn(Optional.of(order));

        OrderDto result = service.handleCallback(callbackCommand());

        assertThat(result.getStatus()).isEqualTo("PAID");
        verify(paymentTransactionPort, never()).findByReference(any());
        verify(paymentTransactionPort, never()).save(any());
        verify(paymentTransactionPort, never()).updateStatus(any(), any(), any(), any());
        verify(orderRepository, never()).save(any());
        verify(inventoryReservationPort, never()).confirm(any());
    }

    @Test
    void callbackPassesRawParametersToPaymentGatewayVerification() {
        Order order = order("order-1", OrderStatus.PAID, "100000.00");
        when(paymentGatewayPort.verify(any())).thenReturn(successfulVerification("order-1", "100000.00"));
        when(orderRepository.findById(new OrderId("order-1"))).thenReturn(Optional.of(order));
        ArgumentCaptor<PaymentGatewayPort.PaymentCallback> callbackCaptor =
            ArgumentCaptor.forClass(PaymentGatewayPort.PaymentCallback.class);

        service.handleCallback(callbackCommand());

        verify(paymentGatewayPort).verify(callbackCaptor.capture());
        assertThat(callbackCaptor.getValue().getParameters())
            .containsEntry("vnp_TxnRef", "order-1")
            .containsEntry("vnp_ResponseCode", "00")
            .containsEntry("vnp_SecureHash", "signed");
    }

    private HandlePaymentCallbackCommand callbackCommand() {
        return HandlePaymentCallbackCommand.builder()
            .parameters(Map.of(
                "vnp_TxnRef", "order-1",
                "vnp_ResponseCode", "00",
                "vnp_SecureHash", "signed"
            ))
            .build();
    }

    private PaymentGatewayPort.PaymentVerification successfulVerification(String orderId, String amount) {
        return PaymentGatewayPort.PaymentVerification.builder()
            .success(true)
            .orderId(orderId)
            .reference(orderId)
            .transactionNo("txn-1")
            .amount(new BigDecimal(amount))
            .rawPayload("{\"ok\":true}")
            .build();
    }

    private Order order(String orderId, OrderStatus status, String totalAmount) {
        return Order.builder()
            .id(new OrderId(orderId))
            .userId(new UserId("buyer-1"))
            .status(status)
            .items(List.of(OrderItem.builder()
                .productId("product-1")
                .variantSku("variant-1")
                .sellerId("seller-1")
                .quantity(1)
                .price(Money.builder().amount(new BigDecimal(totalAmount)).currency("VND").build())
                .build()))
            .totalAmount(Money.builder().amount(new BigDecimal(totalAmount)).currency("VND").build())
            .createdAt(Instant.parse("2026-06-15T00:00:00Z"))
            .updatedAt(Instant.parse("2026-06-15T00:00:00Z"))
            .build();
    }
}
