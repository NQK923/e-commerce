package com.learnfirebase.ecommerce.chat.domain.model;

import com.learnfirebase.ecommerce.common.domain.Identifier;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class ParticipantId extends Identifier {
    private ParticipantId(String value) {
        super(value);
    }

    public static ParticipantId of(String value) {
        return new ParticipantId(value);
    }
}
