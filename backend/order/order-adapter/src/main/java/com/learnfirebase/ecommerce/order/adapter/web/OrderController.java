package com.learnfirebase.ecommerce.order.adapter.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.order.application.command.ApproveReturnCommand;
import com.learnfirebase.ecommerce.order.application.command.CancelOrderCommand;
import com.learnfirebase.ecommerce.order.application.command.CreateOrderCommand;
import com.learnfirebase.ecommerce.order.application.command.MarkDeliveredCommand;
import com.learnfirebase.ecommerce.order.application.command.PayOrderCommand;
import com.learnfirebase.ecommerce.order.application.command.RejectReturnCommand;
import com.learnfirebase.ecommerce.order.application.command.RequestReturnCommand;
import com.learnfirebase.ecommerce.order.application.command.ShipOrderCommand;
import com.learnfirebase.ecommerce.order.application.dto.OrderDto;
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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;
    private final PayOrderUseCase payOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final ListOrdersUseCase listOrdersUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final ShipOrderUseCase shipOrderUseCase;
    private final MarkDeliveredUseCase markDeliveredUseCase;
    private final RequestReturnUseCase requestReturnUseCase;
    private final ApproveReturnUseCase approveReturnUseCase;
    private final RejectReturnUseCase rejectReturnUseCase;

    @GetMapping
    public ResponseEntity<PageResponse<OrderDto>> list(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size,
        @RequestParam(name = "sellerId", required = false) String sellerId,
        Authentication authentication) {
        int safeSize = Math.min(size, 100);
        PageRequest pageRequest = PageRequest.builder().page(page).size(safeSize).build();
        if (authentication == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        if (hasRole(authentication, "ADMIN")) {
            return ResponseEntity.ok(listOrdersUseCase.listOrders(pageRequest, sellerId));
        }
        if (hasRole(authentication, "SELLER")) {
            String effectiveSellerId = sellerId != null ? sellerId : authentication.getName();
            if (!effectiveSellerId.equals(authentication.getName())) {
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You are not authorized to view another seller's orders"
                );
            }
            return ResponseEntity.ok(listOrdersUseCase.listOrders(pageRequest, effectiveSellerId));
        }
        if (sellerId != null) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN, "Customers cannot filter seller orders"
            );
        }
        return ResponseEntity.ok(listOrdersUseCase.listOrdersForUser(pageRequest, authentication.getName()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable("orderId") String orderId, Authentication authentication) {
        OrderDto order = getOrderUseCase.getOrder(orderId);
        if (authentication != null && !canAccessOrder(authentication, order)) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN, "You are not authorized to view this order"
            );
        }
        return ResponseEntity.ok(order);
    }

    @PostMapping
    public ResponseEntity<OrderDto> create(@RequestBody CreateOrderCommand command, Authentication authentication) {
        requireAuthenticated(authentication);
        CreateOrderCommand secureCommand = CreateOrderCommand.builder()
            .userId(authentication.getName())
            .items(command.getItems())
            .currency(command.getCurrency())
            .address(command.getAddress())
            .paymentMethod(command.getPaymentMethod())
            .build();
        return ResponseEntity.ok(createOrderUseCase.execute(secureCommand));
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<OrderDto> pay(@PathVariable("orderId") String orderId, @RequestBody PayOrderCommand command,
        Authentication authentication) {
        requireBuyerOwnerOrAdmin(orderId, authentication);
        PayOrderCommand enriched = PayOrderCommand.builder()
            .orderId(orderId)
            .paymentReference(command.getPaymentReference())
            .build();
        return ResponseEntity.ok(payOrderUseCase.execute(enriched));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDto> cancel(@PathVariable("orderId") String orderId, @RequestBody CancelOrderCommand command,
        Authentication authentication) {
        requireBuyerOwnerOrAdmin(orderId, authentication);
        CancelOrderCommand enriched = CancelOrderCommand.builder()
            .orderId(orderId)
            .reason(command.getReason())
            .build();
        return ResponseEntity.ok(cancelOrderUseCase.execute(enriched));
    }

    @PostMapping("/{orderId}/ship")
    public ResponseEntity<OrderDto> ship(@PathVariable("orderId") String orderId, @RequestBody ShipOrderCommand command,
        Authentication authentication) {
        requireSellerOwnerOrAdmin(orderId, authentication);
        command.setOrderId(orderId);
        return ResponseEntity.ok(shipOrderUseCase.ship(command));
    }

    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<OrderDto> deliver(@PathVariable("orderId") String orderId, Authentication authentication) {
        requireSellerOwnerOrAdmin(orderId, authentication);
        return ResponseEntity.ok(markDeliveredUseCase.markDelivered(MarkDeliveredCommand.builder().orderId(orderId).build()));
    }

    @PostMapping("/{orderId}/return")
    public ResponseEntity<OrderDto> requestReturn(@PathVariable("orderId") String orderId, @RequestBody RequestReturnCommand command,
        Authentication authentication) {
        requireBuyerOwnerOrAdmin(orderId, authentication);
        RequestReturnCommand secureCommand = RequestReturnCommand.builder()
            .orderId(orderId)
            .userId(authentication.getName())
            .reason(command.getReason())
            .note(command.getNote())
            .build();
        return ResponseEntity.ok(requestReturnUseCase.requestReturn(secureCommand));
    }

    @PostMapping("/{orderId}/returns/approve")
    public ResponseEntity<OrderDto> approveReturn(@PathVariable("orderId") String orderId, @RequestBody ApproveReturnCommand command,
        Authentication authentication) {
        requireSellerOwnerOrAdmin(orderId, authentication);
        command.setOrderId(orderId);
        return ResponseEntity.ok(approveReturnUseCase.approveReturn(command));
    }

    @PostMapping("/{orderId}/returns/reject")
    public ResponseEntity<OrderDto> rejectReturn(@PathVariable("orderId") String orderId, @RequestBody RejectReturnCommand command,
        Authentication authentication) {
        requireSellerOwnerOrAdmin(orderId, authentication);
        command.setOrderId(orderId);
        return ResponseEntity.ok(rejectReturnUseCase.rejectReturn(command));
    }

    private void requireAuthenticated(Authentication authentication) {
        if (authentication == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
    }

    private void requireBuyerOwnerOrAdmin(String orderId, Authentication authentication) {
        requireAuthenticated(authentication);
        OrderDto order = getOrderUseCase.getOrder(orderId);
        if (!hasRole(authentication, "ADMIN") && !order.getUserId().equals(authentication.getName())) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN, "You are not authorized to modify this order"
            );
        }
    }

    private void requireSellerOwnerOrAdmin(String orderId, Authentication authentication) {
        requireAuthenticated(authentication);
        OrderDto order = getOrderUseCase.getOrder(orderId);
        if (!hasRole(authentication, "ADMIN")
                && !(hasRole(authentication, "SELLER") && order.getItems().stream()
                    .anyMatch(item -> authentication.getName().equals(item.getSellerId())))) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN, "You are not authorized to fulfill this order"
            );
        }
    }

    private boolean canAccessOrder(Authentication authentication, OrderDto order) {
        if (hasRole(authentication, "ADMIN")) {
            return true;
        }
        if (order.getUserId().equals(authentication.getName())) {
            return true;
        }
        return hasRole(authentication, "SELLER") && order.getItems().stream()
                .anyMatch(item -> authentication.getName().equals(item.getSellerId()));
    }

    private boolean hasRole(Authentication authentication, String role) {
        String authority = "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }
}
