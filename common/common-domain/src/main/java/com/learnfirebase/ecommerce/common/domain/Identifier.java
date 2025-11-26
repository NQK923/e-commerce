package com.learnfirebase.ecommerce.common.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public abstract class Identifier {
    private final String value;

    protected Identifier(String value) {
        this.value = value;
    }
}
