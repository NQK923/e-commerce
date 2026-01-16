package com.learnfirebase.ecommerce.promotion.adapter.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.promotion.application.command.CreateFlashSaleCommand;
import com.learnfirebase.ecommerce.promotion.application.port.in.CreateFlashSaleUseCase;
import com.learnfirebase.ecommerce.promotion.application.port.in.ListFlashSalesUseCase;
import com.learnfirebase.ecommerce.promotion.domain.model.FlashSale;
import com.learnfirebase.ecommerce.promotion.domain.model.FlashSaleId;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FlashSaleController {

    private final CreateFlashSaleUseCase createFlashSaleUseCase;
    private final ListFlashSalesUseCase listFlashSalesUseCase;

    @PostMapping("/admin/flash-sales")
    public ResponseEntity<FlashSaleId> createFlashSale(@RequestBody CreateFlashSaleCommand command) {
        return ResponseEntity.ok(createFlashSaleUseCase.createFlashSale(command));
    }

    @GetMapping("/admin/flash-sales")
    public ResponseEntity<List<FlashSale>> getAllFlashSales() {
        return ResponseEntity.ok(listFlashSalesUseCase.listAllFlashSales());
    }

    @GetMapping("/flash-sales")
    public ResponseEntity<List<FlashSale>> getActiveFlashSales() {
        return ResponseEntity.ok(listFlashSalesUseCase.listActiveFlashSales());
    }
}
