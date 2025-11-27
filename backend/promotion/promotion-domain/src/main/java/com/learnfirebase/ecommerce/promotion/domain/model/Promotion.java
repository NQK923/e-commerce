package com.learnfirebase.ecommerce.promotion.domain.model;

import java.time.Instant;

import com.learnfirebase.ecommerce.common.domain.AggregateRoot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class Promotion extends AggregateRoot<PromotionId> {
    private PromotionId id;
    private String name;
    private PromotionRule rule;
    private Instant startAt;
    private Instant endAt;
}
