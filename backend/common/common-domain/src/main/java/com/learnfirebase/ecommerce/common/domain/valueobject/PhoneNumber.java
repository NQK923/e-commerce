package com.learnfirebase.ecommerce.common.domain.valueobject;

import java.util.regex.Pattern;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PhoneNumber {
    private static final Pattern PATTERN = Pattern.compile("^\\+?[0-9\\- ]{7,20}$");

    String value;

    public PhoneNumber(String value) {
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid phone number: " + value);
        }
        this.value = value;
    }
}
