package com.learnfirebase.ecommerce.common.domain.valueobject;

import java.util.regex.Pattern;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Email {
    private static final Pattern PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    String value;

    public Email(String value) {
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email: " + value);
        }
        this.value = value;
    }
}
