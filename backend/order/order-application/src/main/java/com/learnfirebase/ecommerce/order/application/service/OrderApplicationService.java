package com.learnfirebase.ecommerce.order.application.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.common.domain.valueobject.Money;
import com.learnfirebase.ecommerce.order.application.command.CancelOrderCommand;
import com.learnfirebase.ecommerce.order.application.command.CreateOrderCommand;
import com.learnfirebase.ecommerce.order.application.command.HandlePaymentCallbackCommand;
import com.learnfirebase.ecommerce.order.application.command.InitiatePaymentCommand;
import com.learnfirebase.ecommerce.order.application.command.PayOrderCommand;
import com.learnfirebase.ecommerce.order.application.dto.OrderDto;
import com.learnfirebase.ecommerce.order.application.dto.PaymentInitResponse;
import com.learnfirebase.ecommerce.order.application.model.PaymentRecord;
import com.learnfirebase.ecommerce.order.application.model.PaymentStatus;
import com.learnfirebase.ecommerce.order.application.port.in.HandlePaymentCallbackUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.InitiatePaymentUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.CancelOrderUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.CreateOrderUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.GetOrderUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.ListOrdersUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.PayOrderUseCase;
import com.learnfirebase.ecommerce.order.application.port.out.InventoryReservationPort;
import com.learnfirebase.ecommerce.order.application.port.out.LoadProductPort;
import com.learnfirebase.ecommerce.order.application.port.out.OrderEventPublisher;
import com.learnfirebase.ecommerce.order.application.port.out.OrderOutboxPort;
import com.learnfirebase.ecommerce.order.application.port.out.OrderRepository;
import com.learnfirebase.ecommerce.order.application.port.out.PaymentGatewayPort;
import com.learnfirebase.ecommerce.order.application.port.out.PaymentTransactionPort;
import com.learnfirebase.ecommerce.order.domain.model.Order;
import com.learnfirebase.ecommerce.order.domain.model.OrderId;
import com.learnfirebase.ecommerce.order.domain.model.OrderItem;
import com.learnfirebase.ecommerce.order.domain.model.OrderStatus;
import com.learnfirebase.ecommerce.order.domain.model.UserId;
import com.learnfirebase.ecommerce.order.domain.service.OrderDomainService;
import com.learnfirebase.ecommerce.order.domain.exception.OrderDomainException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderApplicationService implements CreateOrderUseCase, PayOrderUseCase, CancelOrderUseCase, ListOrdersUseCase, GetOrderUseCase, InitiatePaymentUseCase, HandlePaymentCallbackUseCase {

    private final OrderRepository orderRepository;
    private final LoadProductPort loadProductPort;
    private final com.learnfirebase.ecommerce.order.application.port.out.LoadFlashSalePort loadFlashSalePort;
    private final InventoryReservationPort inventoryReservationPort;
    private final OrderOutboxPort orderOutboxPort;
    private final OrderEventPublisher eventPublisher;
    private final PaymentGatewayPort paymentGatewayPort;
    private final PaymentTransactionPort paymentTransactionPort;
    private final OrderDomainService domainService = new OrderDomainService();

    @Override
    public OrderDto execute(CreateOrderCommand command) {
        List<OrderItem> items = new ArrayList<>();
        
        Set<String> standardProductIds = command.getItems().stream()
            .filter(item -> item.getFlashSaleId() == null)
            .map(CreateOrderCommand.OrderItemCommand::getProductId)
            .collect(Collectors.toSet());

        Map<String, String> standardPrices = loadProductPort.loadProductPrices(command.getCurrency(), standardProductIds);

        for (CreateOrderCommand.OrderItemCommand itemCmd : command.getItems()) {
            BigDecimal price;
            if (itemCmd.getFlashSaleId() != null) {
                // Flash Sale: Validate and use Flash Sale price
                var flashSale = loadFlashSalePort.loadFlashSale(itemCmd.getFlashSaleId())
                    .orElseThrow(() -> new OrderDomainException("Flash Sale not found: " + itemCmd.getFlashSaleId()));
                
                if (!flashSale.getProductId().equals(itemCmd.getProductId())) {
                     throw new OrderDomainException("Flash Sale " + itemCmd.getFlashSaleId() + " does not match product " + itemCmd.getProductId());
                }
                
                if (!flashSale.isActive()) {
                     throw new OrderDomainException("Flash Sale " + itemCmd.getFlashSaleId() + " is not active");
                }
                
                if (!flashSale.getPrice().getCurrency().equals(command.getCurrency())) {
                     throw new OrderDomainException("Currency mismatch for Flash Sale " + itemCmd.getFlashSaleId());
                }

                price = flashSale.getPrice().getAmount();
            } else {
                String fetched = standardPrices.get(itemCmd.getProductId());
                price = (fetched != null) ? new BigDecimal(fetched) : new BigDecimal(itemCmd.getPrice());
            }
            
            items.add(OrderItem.builder()
                .productId(itemCmd.getProductId())
                .flashSaleId(itemCmd.getFlashSaleId())
                .quantity(itemCmd.getQuantity())
                .price(Money.builder().amount(price).currency(command.getCurrency()).build())
                .build());
        }

        Order order = domainService.initiateOrder(new UserId(command.getUserId()), items, command.getCurrency());
        Order saved = orderRepository.save(order);

        // Standard Reservations
        Map<String, Integer> standardReservations = command.getItems().stream()
            .filter(i -> i.getFlashSaleId() == null)
            .collect(Collectors.toMap(CreateOrderCommand.OrderItemCommand::getProductId, CreateOrderCommand.OrderItemCommand::getQuantity));
            
        if (!standardReservations.isEmpty()) {
            inventoryReservationPort.reserve(saved.getId().getValue(), standardReservations);
        }

        // Flash Sale Reservations
        command.getItems().stream()
            .filter(i -> i.getFlashSaleId() != null)
            .forEach(i -> {
                 boolean success = inventoryReservationPort.reserveFlashSale(saved.getId().getValue(), i.getFlashSaleId(), i.getQuantity());
                 if (!success) {
                     throw new OrderDomainException("Flash Sale stock exhausted or invalid for product " + i.getProductId());
                 }
            });

        order.getDomainEvents().forEach(event -> {
            orderOutboxPort.saveEvent(event);
            eventPublisher.publish(event);
        });

        return toDto(saved);
    }

    @Override
    public OrderDto execute(PayOrderCommand command) {
        Order order = orderRepository.findById(new OrderId(command.getOrderId()))
            .orElseThrow(() -> new OrderDomainException("Order not found"));
        order.pay();
        Order saved = orderRepository.save(order);
        order.getDomainEvents().forEach(event -> {
            orderOutboxPort.saveEvent(event);
            eventPublisher.publish(event);
        });
        return toDto(saved);
    }

    @Override
    public OrderDto execute(CancelOrderCommand command) {
        Order order = orderRepository.findById(new OrderId(command.getOrderId()))
            .orElseThrow(() -> new OrderDomainException("Order not found"));
        order.cancel(command.getReason());
        Order saved = orderRepository.save(order);
        
        // Release Flash Sale Stock
        saved.getItems().stream()
             .filter(item -> item.getFlashSaleId() != null)
             .forEach(item -> inventoryReservationPort.releaseFlashSale(item.getFlashSaleId(), item.getQuantity()));

        order.getDomainEvents().forEach(event -> {
            orderOutboxPort.saveEvent(event);
            eventPublisher.publish(event);
        });
        return toDto(saved);
    }

    @Override
    public PageResponse<OrderDto> listOrders(PageRequest pageRequest) {
        List<Order> orders = orderRepository.findAll(pageRequest.getPage(), pageRequest.getSize());
        long total = orderRepository.count();
        int totalPages = (int) Math.ceil((double) total / pageRequest.getSize());

        List<OrderDto> dtos = orders.stream().map(this::toDto).collect(Collectors.toList());

        return PageResponse.<OrderDto>builder()
            .content(dtos)
            .page(pageRequest.getPage())
            .size(pageRequest.getSize())
            .totalElements(total)
            .totalPages(totalPages)
            .build();
    }

    @Override
    public OrderDto getOrder(String orderId) {
        Order order = orderRepository.findById(new OrderId(orderId))
            .orElseThrow(() -> new OrderDomainException("Order not found"));
        return toDto(order);
    }

    @Override
    public PaymentInitResponse initiate(InitiatePaymentCommand command) {
        Order order = orderRepository.findById(new OrderId(command.getOrderId()))
            .orElseThrow(() -> new OrderDomainException("Order not found"));

        if (order.getStatus() == OrderStatus.PAID) {
            throw new OrderDomainException("Order already paid");
        }

        PaymentGatewayPort.PaymentSession session = paymentGatewayPort.initiatePayment(
            PaymentGatewayPort.PaymentRequest.builder()
                .orderId(order.getId().getValue())
                .amount(order.getTotalAmount().getAmount())
                .currency(order.getTotalAmount().getCurrency())
                .description("Payment for order " + order.getId().getValue())
                .returnUrl(command.getReturnUrl())
                .clientIp(command.getClientIp())
                .build()
        );

        paymentTransactionPort.save(PaymentRecord.builder()
            .orderId(order.getId().getValue())
            .reference(session.getReference())
            .gateway("VNPAY")
            .amount(order.getTotalAmount().getAmount())
            .currency(order.getTotalAmount().getCurrency())
            .status(PaymentStatus.PENDING)
            .createdAt(java.time.Instant.now())
            .updatedAt(java.time.Instant.now())
            .build());

        return PaymentInitResponse.builder()
            .paymentUrl(session.getPaymentUrl())
            .reference(session.getReference())
            .build();
    }

    @Override
    public OrderDto handleCallback(HandlePaymentCallbackCommand command) {
        PaymentGatewayPort.PaymentVerification verification = paymentGatewayPort.verify(
            PaymentGatewayPort.PaymentCallback.builder()
                .parameters(command.getParameters())
                .build()
        );

        if (!verification.isSuccess()) {
            paymentTransactionPort.updateStatus(verification.getReference(), PaymentStatus.FAILED, verification.getTransactionNo(), verification.getRawPayload());
            throw new OrderDomainException(verification.getErrorMessage() == null ? "Payment failed" : verification.getErrorMessage());
        }

        Order order = orderRepository.findById(new OrderId(verification.getOrderId()))
            .orElseThrow(() -> new OrderDomainException("Order not found"));

        if (paymentTransactionPort.findByReference(verification.getReference()).isEmpty()) {
            paymentTransactionPort.save(PaymentRecord.builder()
                .reference(verification.getReference())
                .orderId(order.getId().getValue())
                .gateway("VNPAY")
                .amount(verification.getAmount())
                .currency(order.getTotalAmount().getCurrency())
                .status(PaymentStatus.PENDING)
                .createdAt(java.time.Instant.now())
                .updatedAt(java.time.Instant.now())
                .rawPayload(verification.getRawPayload())
                .build());
        }

        if (order.getStatus() == OrderStatus.CREATED) {
            order.confirm();
        }
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            order.pay();
        }

        Order saved = orderRepository.save(order);

        paymentTransactionPort.updateStatus(verification.getReference(), PaymentStatus.SUCCESS, verification.getTransactionNo(), verification.getRawPayload());

        saved.getDomainEvents().forEach(event -> {
            orderOutboxPort.saveEvent(event);
            eventPublisher.publish(event);
        });

        return toDto(saved);
    }

    private OrderDto toDto(Order order) {
        return OrderDto.builder()
            .id(order.getId().getValue())
            .userId(order.getUserId().getValue())
            .status(order.getStatus().name())
            .currency(order.getTotalAmount().getCurrency())
            .totalAmount(order.getTotalAmount().getAmount().toPlainString())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .items(order.getItems().stream()
                .map(item -> OrderDto.OrderItemDto.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .price(item.getPrice().getAmount().toPlainString())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
