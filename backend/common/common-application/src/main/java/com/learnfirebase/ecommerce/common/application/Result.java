package com.learnfirebase.ecommerce.common.application;

import java.util.Optional;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Result<T> {
    private final T value;
    private final String error;

    public static <T> Result<T> success(T value) {
        return new Result<>(value, null);
    }

    public static <T> Result<T> failure(String error) {
        return new Result<>(null, error);
    }

    public boolean isSuccess() {
        return error == null;
    }

    public Optional<T> getValue() {
        return Optional.ofNullable(value);
    }

    public <R> Result<R> map(Function<T, R> mapper) {
        if (!isSuccess() || value == null) {
            return Result.failure(error);
        }
        return Result.success(mapper.apply(value));
    }
}
