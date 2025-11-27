package com.learnfirebase.ecommerce.notification.infrastructure.gateway;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.notification.application.port.out.EmailGateway;

@Component
public class EmailGatewayImpl implements EmailGateway {
    @Override
    public String send(String recipient, String subject, String body) {
        return "email-" + recipient;
    }
}
