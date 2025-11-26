package com.learnfirebase.ecommerce.promotion.adapter.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.promotion.application.command.ApplyPromotionCommand;
import com.learnfirebase.ecommerce.promotion.application.dto.PromotionResultDto;
import com.learnfirebase.ecommerce.promotion.application.port.in.ApplyPromotionUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {
    private final ApplyPromotionUseCase applyPromotionUseCase;

    @PostMapping("/apply")
    public ResponseEntity<PromotionResultDto> apply(@RequestBody ApplyPromotionCommand command) {
        return ResponseEntity.ok(applyPromotionUseCase.execute(command));
    }
}
