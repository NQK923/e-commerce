import { apiRequest } from "../lib/api-client";
import { buildQueryString } from "../lib/query-string";
import { ChatMessage, ConversationSummary } from "../types/chat";

type MessageDto = {
  id: string;
  conversationId: string;
  senderId: string;
  receiverId: string;
  content: string;
  sentAt: string;
  status: "PENDING" | "DELIVERED" | "READ";
};

type ConversationDto = {
  id: string;
  participants: Array<{
    id: string;
    displayName?: string;
    avatarUrl?: string;
    role?: string;
  }>;
  lastMessage?: MessageDto;
  unreadCount?: number;
  createdAt?: string;
};

const mapMessage = (dto: MessageDto): ChatMessage => ({
  id: dto.id,
  conversationId: dto.conversationId,
  senderId: dto.senderId,
  receiverId: dto.receiverId,
  content: dto.content,
  sentAt: dto.sentAt,
  status: dto.status,
});

const mapConversation = (dto: ConversationDto): ConversationSummary => ({
  id: dto.id,
  participants: dto.participants ?? [],
  lastMessage: dto.lastMessage ? mapMessage(dto.lastMessage) : undefined,
  unreadCount: dto.unreadCount ?? 0,
  createdAt: dto.createdAt,
});

export const chatApi = {
  async listConversations(): Promise<ConversationSummary[]> {
    const data = await apiRequest<ConversationDto[]>("/api/chat/conversations");
    return data.map(mapConversation);
  },

  async getConversation(conversationId: string): Promise<ConversationSummary> {
    const dto = await apiRequest<ConversationDto>(`/api/chat/conversations/${conversationId}`);
    return mapConversation(dto);
  },

  async listMessages(conversationId: string, limit = 50): Promise<ChatMessage[]> {
    const qs = buildQueryString({ limit });
    const data = await apiRequest<MessageDto[]>(
      `/api/chat/conversations/${conversationId}/messages${qs}`,
    );
    return data.map(mapMessage).sort((a, b) => a.sentAt.localeCompare(b.sentAt));
  },

  async markAsRead(conversationId: string): Promise<void> {
    await apiRequest<void>(`/api/chat/conversations/${conversationId}/read`, { method: "POST" });
  },
};
