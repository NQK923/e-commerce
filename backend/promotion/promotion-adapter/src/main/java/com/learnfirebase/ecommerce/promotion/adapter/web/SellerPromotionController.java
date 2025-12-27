package com.learnfirebase.ecommerce.promotion.adapter.web;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.learnfirebase.ecommerce.promotion.application.command.CreateCouponCommand;
import com.learnfirebase.ecommerce.promotion.application.service.SellerPromotionApplicationService;
import com.learnfirebase.ecommerce.promotion.domain.model.Coupon;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/seller/promotions")
@RequiredArgsConstructor
public class SellerPromotionController {

    private final SellerPromotionApplicationService sellerPromotionService;

    @PostMapping("/coupons")
    public ResponseEntity<Coupon> createCoupon(@RequestBody CreateCouponCommand command, Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        // Force sellerId from token
        CreateCouponCommand secureCommand = CreateCouponCommand.builder()
            .code(command.getCode())
            .sellerId(principal.getName()) // Enforce ownership
            .applicableProductIds(command.getApplicableProductIds())
            .discountType(command.getDiscountType())
            .discountValue(command.getDiscountValue())
            .minOrderAmount(command.getMinOrderAmount())
            .currency(command.getCurrency())
            .maxDiscountAmount(command.getMaxDiscountAmount())
            .usageLimit(command.getUsageLimit())
            .startAt(command.getStartAt())
            .endAt(command.getEndAt())
            .build();
            
        return ResponseEntity.ok(sellerPromotionService.createCoupon(secureCommand));
    }

    @GetMapping("/coupons")
    public ResponseEntity<List<Coupon>> listCoupons(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(sellerPromotionService.getSellerCoupons(principal.getName()));
    }
}
