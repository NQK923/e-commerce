package com.learnfirebase.ecommerce.chat.application.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConversationSummaryDto {
    String id;
    List<ParticipantDto> participants;
    ChatMessageDto lastMessage;
    Integer unreadCount;
    Instant createdAt;

    @Value
    @Builder
    public static class ParticipantDto {
        String id;
        String displayName;
        String avatarUrl;
        String role;
    }
}
