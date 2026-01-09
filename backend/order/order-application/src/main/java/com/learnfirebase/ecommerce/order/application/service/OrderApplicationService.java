package com.learnfirebase.ecommerce.order.application.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.common.domain.valueobject.Money;
import com.learnfirebase.ecommerce.order.application.command.ApproveReturnCommand;
import com.learnfirebase.ecommerce.order.application.command.CancelOrderCommand;
import com.learnfirebase.ecommerce.order.application.command.CreateOrderCommand;
import com.learnfirebase.ecommerce.order.application.command.HandlePaymentCallbackCommand;
import com.learnfirebase.ecommerce.order.application.command.InitiatePaymentCommand;
import com.learnfirebase.ecommerce.order.application.command.MarkDeliveredCommand;
import com.learnfirebase.ecommerce.order.application.command.PayOrderCommand;
import com.learnfirebase.ecommerce.order.application.command.RejectReturnCommand;
import com.learnfirebase.ecommerce.order.application.command.RequestReturnCommand;
import com.learnfirebase.ecommerce.order.application.command.ShipOrderCommand;
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
import com.learnfirebase.ecommerce.order.application.port.in.ShipOrderUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.MarkDeliveredUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.RequestReturnUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.ApproveReturnUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.RejectReturnUseCase;
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
import com.learnfirebase.ecommerce.order.domain.model.ReturnStatus;
import com.learnfirebase.ecommerce.order.domain.service.OrderDomainService;
import com.learnfirebase.ecommerce.order.domain.exception.OrderDomainException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderApplicationService implements CreateOrderUseCase, PayOrderUseCase, CancelOrderUseCase, ListOrdersUseCase, GetOrderUseCase, InitiatePaymentUseCase, HandlePaymentCallbackUseCase, ShipOrderUseCase, MarkDeliveredUseCase, RequestReturnUseCase, ApproveReturnUseCase, RejectReturnUseCase {

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
        
        Set<String> allProductIds = command.getItems().stream()
            .map(CreateOrderCommand.OrderItemCommand::getProductId)
            .collect(Collectors.toSet());

        Map<String, LoadProductPort.ProductInfo> productsInfo = loadProductPort.loadProducts(command.getCurrency(), allProductIds);

        for (CreateOrderCommand.OrderItemCommand itemCmd : command.getItems()) {
            LoadProductPort.ProductInfo productInfo = productsInfo.get(itemCmd.getProductId());
            if (productInfo == null) {
                throw new OrderDomainException("Product not found: " + itemCmd.getProductId());
            }

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
                price = productInfo.getPrice();
            }
            
            items.add(OrderItem.builder()
                .productId(itemCmd.getProductId())
                .variantSku(itemCmd.getVariantSku())
                .flashSaleId(itemCmd.getFlashSaleId())
                .sellerId(productInfo.getSellerId())
                .quantity(itemCmd.getQuantity())
                .price(Money.builder().amount(price).currency(command.getCurrency()).build())
                .build());
        }

        Order order = domainService.initiateOrder(new UserId(command.getUserId()), items, command.getCurrency());
        Order saved = orderRepository.save(order);

        // Standard Reservations
        Map<String, Integer> standardReservations = buildReservationMap(order.getItems().stream()
            .filter(i -> i.getFlashSaleId() == null)
            .toList());
            
        if (!standardReservations.isEmpty()) {
            boolean ok = inventoryReservationPort.reserve(saved.getId().getValue(), standardReservations);
            if (!ok) {
                throw new OrderDomainException("Insufficient stock for one or more items");
            }
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
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderDomainException("Cannot pay a cancelled order");
        }
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.SHIPPING || order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.RETURNED) {
            return toDto(order);
        }
        order.pay();
        Order saved = orderRepository.save(order);
        inventoryReservationPort.confirm(order.getId().getValue());
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
        Map<String, Integer> standardReservations = buildReservationMap(saved.getItems().stream()
            .filter(item -> item.getFlashSaleId() == null)
            .toList());
        if (!standardReservations.isEmpty()) {
            inventoryReservationPort.release(order.getId().getValue(), standardReservations);
        }

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
    public PageResponse<OrderDto> listOrders(PageRequest pageRequest, String sellerId) {
        if (sellerId != null) {
            List<Order> orders = orderRepository.findBySellerId(sellerId, pageRequest.getPage(), pageRequest.getSize());
            long total = orderRepository.countBySellerId(sellerId);
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
        return listOrders(pageRequest);
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

        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.SHIPPING || order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.RETURNED) {
            throw new OrderDomainException("Order already paid");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderDomainException("Cancelled order cannot be paid");
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
            
        // Idempotency check: If order is already paid, ignore duplicate callback
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.SHIPPING || 
            order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.RETURNED) {
            return toDto(order);
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderDomainException("Payment callback for cancelled order");
        }

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

        if (order.getStatus() == OrderStatus.PENDING) {
            order.pay();
        }

        Order saved = orderRepository.save(order);

        paymentTransactionPort.updateStatus(verification.getReference(), PaymentStatus.SUCCESS, verification.getTransactionNo(), verification.getRawPayload());
        inventoryReservationPort.confirm(order.getId().getValue());

        saved.getDomainEvents().forEach(event -> {
            orderOutboxPort.saveEvent(event);
            eventPublisher.publish(event);
        });

        return toDto(saved);
    }

    @Override
    public OrderDto ship(ShipOrderCommand command) {
        Order order = orderRepository.findById(new OrderId(command.getOrderId()))
            .orElseThrow(() -> new OrderDomainException("Order not found"));
        order.ship(command.getTrackingNumber(), command.getTrackingCarrier());
        Order saved = orderRepository.save(order);
        return toDto(saved);
    }

    @Override
    public OrderDto markDelivered(MarkDeliveredCommand command) {
        Order order = orderRepository.findById(new OrderId(command.getOrderId()))
            .orElseThrow(() -> new OrderDomainException("Order not found"));
        order.markDelivered();
        Order saved = orderRepository.save(order);
        return toDto(saved);
    }

    @Override
    public OrderDto requestReturn(RequestReturnCommand command) {
        Order order = orderRepository.findById(new OrderId(command.getOrderId()))
            .orElseThrow(() -> new OrderDomainException("Order not found"));
        if (order.getUserId() != null && command.getUserId() != null && !order.getUserId().getValue().equals(command.getUserId())) {
            throw new OrderDomainException("Only order owner can request a return");
        }
        order.requestReturn(command.getReason(), command.getNote());
        Order saved = orderRepository.save(order);
        return toDto(saved);
    }

    @Override
    public OrderDto approveReturn(ApproveReturnCommand command) {
        Order order = orderRepository.findById(new OrderId(command.getOrderId()))
            .orElseThrow(() -> new OrderDomainException("Order not found"));
        String currency = command.getCurrency() != null ? command.getCurrency() : order.getTotalAmount().getCurrency();
        java.math.BigDecimal refundAmount = command.getRefundAmount() != null && !command.getRefundAmount().isBlank()
            ? new java.math.BigDecimal(command.getRefundAmount())
            : order.getTotalAmount().getAmount();
        
        // Business Rule: We do NOT release inventory back for returned items because they are considered 'used' or 'damaged'.
        // If this rule changes (e.g. for 'refused delivery'), call inventoryReservationPort.release() here.
        
        order.approveReturn(Money.builder().amount(refundAmount).currency(currency).build(), command.getNote());
        Order saved = orderRepository.save(order);
        return toDto(saved);
    }

    @Override
    public OrderDto rejectReturn(RejectReturnCommand command) {
        Order order = orderRepository.findById(new OrderId(command.getOrderId()))
            .orElseThrow(() -> new OrderDomainException("Order not found"));
        order.rejectReturn(command.getNote());
        Order saved = orderRepository.save(order);
        return toDto(saved);
    }

    private OrderDto toDto(Order order) {
        return OrderDto.builder()
            .id(order.getId().getValue())
            .userId(order.getUserId().getValue())
            .status(order.getStatus().name())
            .currency(order.getTotalAmount().getCurrency())
            .totalAmount(order.getTotalAmount().getAmount().toPlainString())
            .trackingNumber(order.getTrackingNumber())
            .trackingCarrier(order.getTrackingCarrier())
            .shippedAt(order.getShippedAt())
            .deliveredAt(order.getDeliveredAt())
            .returnStatus(order.getReturnStatus() != null ? order.getReturnStatus().name() : ReturnStatus.NONE.name())
            .returnReason(order.getReturnReason())
            .returnNote(order.getReturnNote())
            .returnRequestedAt(order.getReturnRequestedAt())
            .returnResolvedAt(order.getReturnResolvedAt())
            .refundAmount(order.getRefundAmount() != null ? order.getRefundAmount().getAmount().toPlainString() : null)
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .items(order.getItems().stream()
                .map(item -> OrderDto.OrderItemDto.builder()
                    .productId(item.getProductId())
                    .variantSku(item.getVariantSku())
                    .flashSaleId(item.getFlashSaleId())
                    .quantity(item.getQuantity())
                    .price(item.getPrice().getAmount().toPlainString())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }

    private Map<String, Integer> buildReservationMap(List<OrderItem> items) {
        Map<String, Integer> reservations = new HashMap<>();
        for (OrderItem item : items) {
            String key = resolveInventoryKey(item.getProductId(), item.getVariantSku());
            reservations.merge(key, item.getQuantity(), Integer::sum);
        }
        return reservations;
    }

    private String resolveInventoryKey(CreateOrderCommand.OrderItemCommand itemCmd) {
        return resolveInventoryKey(itemCmd.getProductId(), itemCmd.getVariantSku());
    }

    private String resolveInventoryKey(String productId, String variantSku) {
        return variantSku != null && !variantSku.isBlank() ? variantSku : productId;
    }
}
