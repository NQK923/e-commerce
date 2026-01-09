package com.learnfirebase.ecommerce.order.adapter.web;

import org.springframework.http.ResponseEntity;
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
        @RequestParam(name = "sellerId", required = false) String sellerId) {
        int safeSize = Math.min(size, 100);
        PageRequest pageRequest = PageRequest.builder().page(page).size(safeSize).build();
        return ResponseEntity.ok(listOrdersUseCase.listOrders(pageRequest, sellerId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable("orderId") String orderId, java.security.Principal principal) {
        OrderDto order = getOrderUseCase.getOrder(orderId);
        if (principal != null && !order.getUserId().equals(principal.getName())) {
             throw new org.springframework.web.server.ResponseStatusException(
                 org.springframework.http.HttpStatus.FORBIDDEN, "You are not authorized to view this order"
             );
        }
        return ResponseEntity.ok(order);
    }

    @PostMapping
    public ResponseEntity<OrderDto> create(@RequestBody CreateOrderCommand command) {
        return ResponseEntity.ok(createOrderUseCase.execute(command));
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<OrderDto> pay(@PathVariable("orderId") String orderId, @RequestBody PayOrderCommand command) {
        PayOrderCommand enriched = PayOrderCommand.builder()
            .orderId(orderId)
            .paymentReference(command.getPaymentReference())
            .build();
        return ResponseEntity.ok(payOrderUseCase.execute(enriched));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDto> cancel(@PathVariable("orderId") String orderId, @RequestBody CancelOrderCommand command) {
        CancelOrderCommand enriched = CancelOrderCommand.builder()
            .orderId(orderId)
            .reason(command.getReason())
            .build();
        return ResponseEntity.ok(cancelOrderUseCase.execute(enriched));
    }

    @PostMapping("/{orderId}/ship")
    public ResponseEntity<OrderDto> ship(@PathVariable("orderId") String orderId, @RequestBody ShipOrderCommand command) {
        command.setOrderId(orderId);
        return ResponseEntity.ok(shipOrderUseCase.ship(command));
    }

    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<OrderDto> deliver(@PathVariable("orderId") String orderId) {
        return ResponseEntity.ok(markDeliveredUseCase.markDelivered(MarkDeliveredCommand.builder().orderId(orderId).build()));
    }

    @PostMapping("/{orderId}/return")
    public ResponseEntity<OrderDto> requestReturn(@PathVariable("orderId") String orderId, @RequestBody RequestReturnCommand command) {
        command.setOrderId(orderId);
        return ResponseEntity.ok(requestReturnUseCase.requestReturn(command));
    }

    @PostMapping("/{orderId}/returns/approve")
    public ResponseEntity<OrderDto> approveReturn(@PathVariable("orderId") String orderId, @RequestBody ApproveReturnCommand command) {
        command.setOrderId(orderId);
        return ResponseEntity.ok(approveReturnUseCase.approveReturn(command));
    }

    @PostMapping("/{orderId}/returns/reject")
    public ResponseEntity<OrderDto> rejectReturn(@PathVariable("orderId") String orderId, @RequestBody RejectReturnCommand command) {
        command.setOrderId(orderId);
        return ResponseEntity.ok(rejectReturnUseCase.rejectReturn(command));
    }
}
