package com.learnfirebase.ecommerce.chat.domain.model;

import com.learnfirebase.ecommerce.common.domain.Identifier;
import java.util.UUID;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class ConversationId extends Identifier {
    private ConversationId(String value) {
        super(value);
    }

    public static ConversationId of(String value) {
        return new ConversationId(value);
    }

    public static ConversationId newId() {
        return new ConversationId(UUID.randomUUID().toString());
    }
}
