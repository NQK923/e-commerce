package com.learnfirebase.ecommerce.common.application;

public interface UseCase<I, O> {
    O execute(I input);
}
