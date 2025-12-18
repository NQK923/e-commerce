package com.learnfirebase.ecommerce.identity.application.port.out;

public interface OtpSender {
    void sendOtp(String email, String code);
}
