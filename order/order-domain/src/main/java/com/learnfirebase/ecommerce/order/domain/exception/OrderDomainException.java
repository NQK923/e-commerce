package com.learnfirebase.ecommerce.order.domain.exception;

import com.learnfirebase.ecommerce.common.domain.DomainException;

public class OrderDomainException extends DomainException {
    public OrderDomainException(String message) {
        super(message);
    }
}
