package com.learnfirebase.ecommerce.chat.application.port.in;

import com.learnfirebase.ecommerce.chat.application.dto.ConversationSummaryDto;
import java.util.List;

public interface GetConversationsUseCase {
    List<ConversationSummaryDto> listForUser(String userId);
}
