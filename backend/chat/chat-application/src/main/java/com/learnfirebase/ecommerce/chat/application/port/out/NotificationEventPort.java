package com.learnfirebase.ecommerce.chat.application.port.out;

import com.learnfirebase.ecommerce.chat.domain.model.Message;

public interface NotificationEventPort {
    void publishOfflineMessage(Message message);
}
