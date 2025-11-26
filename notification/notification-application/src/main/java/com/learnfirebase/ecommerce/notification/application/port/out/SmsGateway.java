package com.learnfirebase.ecommerce.notification.application.port.out;

public interface SmsGateway {
    String send(String recipient, String body);
}
