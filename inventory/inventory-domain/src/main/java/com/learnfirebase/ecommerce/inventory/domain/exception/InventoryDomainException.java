package com.learnfirebase.ecommerce.inventory.domain.exception;

import com.learnfirebase.ecommerce.common.domain.DomainException;

public class InventoryDomainException extends DomainException {
    public InventoryDomainException(String message) {
        super(message);
    }
}
