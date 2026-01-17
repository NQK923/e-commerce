export type MessageStatus = "PENDING" | "DELIVERED" | "READ";

export type ChatMessage = {
  id: string;
  conversationId: string;
  senderId: string;
  receiverId: string;
  content: string;
  sentAt: string;
  status: MessageStatus;
};

export type ChatParticipant = {
  id: string;
  displayName?: string;
  avatarUrl?: string;
  role?: string;
  storeName?: string;
  online?: boolean;
  lastActiveAt?: string;
};

export type ConversationSummary = {
  id: string;
  participants: ChatParticipant[];
  lastMessage?: ChatMessage;
  unreadCount?: number;
  createdAt?: string;
};

export type SendChatMessagePayload = {
  conversationId?: string;
  receiverId: string;
  content: string;
};

export type SendChatMessageResult = {
  persistedMessage: ChatMessage;
  receiverOnline: boolean;
};

export type ChatPresenceEvent = {
  userId: string;
  online: boolean;
  lastActiveAt: string;
};

export type TypingEvent = {
  conversationId: string;
  senderId: string;
  receiverId: string;
  typing: boolean;
};
