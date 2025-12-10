package com.learnfirebase.ecommerce.chat.application.port.out;

public interface PresencePort {
    boolean isOnline(String userId);
}
