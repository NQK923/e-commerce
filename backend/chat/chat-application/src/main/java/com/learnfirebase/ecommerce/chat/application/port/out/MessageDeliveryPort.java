package com.learnfirebase.ecommerce.chat.application.port.out;

import com.learnfirebase.ecommerce.chat.domain.model.Message;

public interface MessageDeliveryPort {
    void deliverToUser(String destinationUserId, Message message);
}
