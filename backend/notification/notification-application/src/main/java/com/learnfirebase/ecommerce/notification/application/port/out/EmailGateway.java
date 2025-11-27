package com.learnfirebase.ecommerce.notification.application.port.out;

public interface EmailGateway {
    String send(String recipient, String subject, String body);
}
