package com.learnfirebase.ecommerce.logistics.adapter.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.logistics.application.command.CreateShipmentCommand;
import com.learnfirebase.ecommerce.logistics.application.dto.ShipmentDto;
import com.learnfirebase.ecommerce.logistics.application.port.in.CreateShipmentUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class LogisticsController {
    private final CreateShipmentUseCase createShipmentUseCase;

    @PostMapping
    public ResponseEntity<ShipmentDto> create(@RequestBody CreateShipmentCommand command) {
        return ResponseEntity.ok(createShipmentUseCase.execute(command));
    }
}
