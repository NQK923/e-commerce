-- chat schema
CREATE TABLE chat_conversation (
    id VARCHAR(255) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE chat_conversation_participants (
    conversation_id VARCHAR(255) NOT NULL REFERENCES chat_conversation(id) ON DELETE CASCADE,
    participant_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (conversation_id, participant_id)
);

CREATE TABLE chat_message (
    id VARCHAR(255) PRIMARY KEY,
    conversation_id VARCHAR(255) NOT NULL REFERENCES chat_conversation(id) ON DELETE CASCADE,
    sender_id VARCHAR(255) NOT NULL,
    receiver_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    status VARCHAR(32) NOT NULL
);

CREATE INDEX idx_chat_message_conversation_sent_at ON chat_message(conversation_id, sent_at);
