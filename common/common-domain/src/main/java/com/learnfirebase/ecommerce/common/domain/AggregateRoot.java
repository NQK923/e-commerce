package com.learnfirebase.ecommerce.common.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;

@Getter
public abstract class AggregateRoot<ID extends Identifier> {
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    protected void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public void clearEvents() {
        this.domainEvents.clear();
    }
}
