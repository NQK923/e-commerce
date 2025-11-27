package com.learnfirebase.ecommerce.notification.infrastructure.gateway;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.notification.application.port.out.PushGateway;

@Component
public class PushGatewayImpl implements PushGateway {
    @Override
    public String send(String recipient, String body) {
        return "push-" + recipient;
    }
}
