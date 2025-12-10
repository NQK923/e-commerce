package com.learnfirebase.ecommerce.chat.domain.model;

import com.learnfirebase.ecommerce.common.domain.Identifier;
import java.util.UUID;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class MessageId extends Identifier {
    private MessageId(String value) {
        super(value);
    }

    public static MessageId of(String value) {
        return new MessageId(value);
    }

    public static MessageId newId() {
        return new MessageId(UUID.randomUUID().toString());
    }
}
