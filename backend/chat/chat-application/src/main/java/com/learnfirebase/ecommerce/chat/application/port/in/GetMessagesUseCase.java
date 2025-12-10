package com.learnfirebase.ecommerce.chat.application.port.in;

import com.learnfirebase.ecommerce.chat.application.dto.ChatMessageDto;
import java.util.List;

public interface GetMessagesUseCase {
    List<ChatMessageDto> listMessages(String conversationId, int limit);
}
