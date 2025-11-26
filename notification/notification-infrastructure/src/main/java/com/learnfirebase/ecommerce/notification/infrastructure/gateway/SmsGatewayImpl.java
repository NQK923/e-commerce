package com.learnfirebase.ecommerce.notification.infrastructure.gateway;

import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.notification.application.port.out.SmsGateway;

@Component
public class SmsGatewayImpl implements SmsGateway {
    @Override
    public String send(String recipient, String body) {
        return "sms-" + recipient;
    }
}
