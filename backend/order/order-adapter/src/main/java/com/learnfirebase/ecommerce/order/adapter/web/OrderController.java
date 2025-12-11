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
import com.learnfirebase.ecommerce.order.application.command.CancelOrderCommand;
import com.learnfirebase.ecommerce.order.application.command.CreateOrderCommand;
import com.learnfirebase.ecommerce.order.application.command.PayOrderCommand;
import com.learnfirebase.ecommerce.order.application.dto.OrderDto;
import com.learnfirebase.ecommerce.order.application.port.in.CancelOrderUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.CreateOrderUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.GetOrderUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.ListOrdersUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.PayOrderUseCase;

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

    @GetMapping
    public ResponseEntity<PageResponse<OrderDto>> list(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.builder().page(page).size(size).build();
        return ResponseEntity.ok(listOrdersUseCase.listOrders(pageRequest));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable("orderId") String orderId) {
        return ResponseEntity.ok(getOrderUseCase.getOrder(orderId));
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
}
