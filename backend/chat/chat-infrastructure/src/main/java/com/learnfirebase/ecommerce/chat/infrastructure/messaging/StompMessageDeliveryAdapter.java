package com.learnfirebase.ecommerce.chat.infrastructure.messaging;

import com.learnfirebase.ecommerce.chat.application.port.out.MessageDeliveryPort;
import com.learnfirebase.ecommerce.chat.domain.model.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class StompMessageDeliveryAdapter implements MessageDeliveryPort {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public StompMessageDeliveryAdapter(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public void deliverToUser(String destinationUserId, Message message) {
        String destination = "/queue/chat/messages";
        simpMessagingTemplate.convertAndSendToUser(destinationUserId, destination, message);
    }
}
