package com.learnfirebase.ecommerce.chat.domain.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@Builder
public class Message {
    @NonNull
    private final MessageId id;
    @NonNull
    private final ConversationId conversationId;
    @NonNull
    private final ParticipantId senderId;
    @NonNull
    private final ParticipantId receiverId;
    @NonNull
    private final String content;
    @NonNull
    private final Instant sentAt;
    @Builder.Default
    private final MessageStatus status = MessageStatus.PENDING;

    public Message delivered() {
        return Message.builder()
                .id(id)
                .conversationId(conversationId)
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .sentAt(sentAt)
                .status(MessageStatus.DELIVERED)
                .build();
    }
}
