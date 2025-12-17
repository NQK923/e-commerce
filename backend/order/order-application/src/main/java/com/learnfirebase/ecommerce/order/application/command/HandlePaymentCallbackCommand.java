package com.learnfirebase.ecommerce.order.application.command;

import java.util.Map;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HandlePaymentCallbackCommand {
    Map<String, String> parameters;
}
