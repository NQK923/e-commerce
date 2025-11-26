package com.learnfirebase.ecommerce.notification.application.port.out;

public interface PushGateway {
    String send(String recipient, String body);
}
