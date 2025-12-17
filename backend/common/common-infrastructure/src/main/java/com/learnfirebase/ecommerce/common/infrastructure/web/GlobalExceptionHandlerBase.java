package com.learnfirebase.ecommerce.common.infrastructure.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.learnfirebase.ecommerce.common.domain.DomainException;
import com.learnfirebase.ecommerce.common.infrastructure.logging.LoggingUtils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.Builder;
import lombok.Value;

@RestControllerAdvice
public class GlobalExceptionHandlerBase {
    private final Logger log = LoggingUtils.logger(GlobalExceptionHandlerBase.class);

    @ExceptionHandler(DomainException.class)
    public ErrorResponse handleDomainException(DomainException ex) {
        log.warn("Domain validation error", ex);
        return ErrorResponse.of("DOMAIN_ERROR", ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fieldError -> fieldError.getDefaultMessage() == null ? "Invalid value" : fieldError.getDefaultMessage(),
                (first, second) -> first,
                LinkedHashMap::new
            ));
        log.debug("Request validation failed {}", errors);
        return ErrorResponse.of("VALIDATION_ERROR", "Invalid request", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
            .collect(Collectors.toMap(
                violation -> violation.getPropertyPath().toString(),
                violation -> violation.getMessage() == null ? "Invalid value" : violation.getMessage(),
                (first, second) -> first,
                LinkedHashMap::new
            ));
        log.debug("Constraint validation failed {}", errors);
        return ErrorResponse.of("VALIDATION_ERROR", "Invalid request", errors);
    }

    @ExceptionHandler(Exception.class)
    public ErrorResponse handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ErrorResponse.of("INTERNAL_ERROR", "Internal error", null);
    }

    @Value
    @Builder
    public static class ErrorResponse {
        String code;
        String message;
        Map<String, String> details;

        public static ErrorResponse of(String code, String message, Map<String, String> details) {
            return ErrorResponse.builder()
                .code(code)
                .message(message)
                .details(details == null || details.isEmpty() ? null : details)
                .build();
        }
    }
}
