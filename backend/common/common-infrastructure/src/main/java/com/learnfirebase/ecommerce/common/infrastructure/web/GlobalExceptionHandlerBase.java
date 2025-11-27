package com.learnfirebase.ecommerce.common.infrastructure.web;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.learnfirebase.ecommerce.common.domain.DomainException;
import com.learnfirebase.ecommerce.common.infrastructure.logging.LoggingUtils;

@RestControllerAdvice
public class GlobalExceptionHandlerBase {
    private final Logger log = LoggingUtils.logger(GlobalExceptionHandlerBase.class);

    @ExceptionHandler(DomainException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleDomainException(DomainException ex) {
        log.warn("Domain validation error", ex);
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error");
    }
}
