'use client';

import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { chatApi } from "../api/chatApi";
import { ChatConnectionStatus, useChatClient } from "../hooks/use-chat-client";
import {
  ChatMessage,
  ConversationSummary,
  ChatPresenceEvent,
  SendChatMessagePayload,
  SendChatMessageResult,
} from "../types/chat";
import { useAuth } from "./auth-store";
import { useToast } from "../components/ui/toast-provider";

type ChatContextValue = {
  conversations: ConversationSummary[];
  activeConversationId: string | null;
  setActiveConversationId: (conversationId: string | null) => void;
  getMessagesForConversation: (conversationId: string | null | undefined) => ChatMessage[];
  getPresenceForUser: (userId?: string | null) => { online: boolean; lastActiveAt: string } | undefined;
  sendMessage: (payload: SendChatMessagePayload) => Promise<void>;
  connectionStatus: ChatConnectionStatus;
  loadingConversations: boolean;
  loadingMessages: boolean;
  refreshConversations: () => Promise<void>;
};

const ChatContext = createContext<ChatContextValue | undefined>(undefined);

const tempConversationKey = (userId: string) => `temp:${userId}`;

const mergeMessages = (existing: ChatMessage[], incoming: ChatMessage): ChatMessage[] => {
  const replaced = existing.map((msg) => (msg.id === incoming.id ? incoming : msg));
  const exists = replaced.some((msg) => msg.id === incoming.id);
  const next = exists ? replaced : [...existing, incoming];
  return next.sort((a, b) => a.sentAt.localeCompare(b.sentAt));
};

export const ChatProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, accessToken } = useAuth();
  const { addToast } = useToast();
  const [conversations, setConversations] = useState<ConversationSummary[]>([]);
  const [messagesByConversation, setMessagesByConversation] = useState<
    Record<string, ChatMessage[]>
  >({});
  const [activeConversationId, setActiveConversationId] = useState<string | null>(null);
  const [loadingConversations, setLoadingConversations] = useState(false);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [connectionStatus, setConnectionStatus] = useState<ChatConnectionStatus>("idle");
  const [presenceByUser, setPresenceByUser] = useState<
    Record<string, { online: boolean; lastActiveAt: string }>
  >({});

  const updatePresence = useCallback((userId: string, online: boolean) => {
    if (!userId) return;
    setPresenceByUser((prev) => ({
      ...prev,
      [userId]: { online, lastActiveAt: new Date().toISOString() },
    }));
  }, []);

  const getPresenceForUser = useCallback(
    (userId?: string | null) => {
      if (!userId) return undefined;
      return presenceByUser[userId];
    },
    [presenceByUser],
  );

  const resetState = useCallback(() => {
    setConversations([]);
    setMessagesByConversation({});
    setActiveConversationId(null);
    setPresenceByUser({});
  }, []);

  const getMessagesForConversation = useCallback(
    (conversationId: string | null | undefined) => {
      if (!conversationId) return [];
      return messagesByConversation[conversationId] ?? [];
    },
    [messagesByConversation],
  );

  const refreshConversations = useCallback(async () => {
    if (!user) return;
    setLoadingConversations(true);
    try {
      const items = await chatApi.listConversations();
      setConversations(items);
      setActiveConversationId((current) => current ?? items[0]?.id ?? null);
    } catch {
      addToast("Failed to load conversations", "error");
    } finally {
      setLoadingConversations(false);
    }
  }, [addToast, user]);

  useEffect(() => {
    if (!user) {
      resetState();
      return;
    }
    void refreshConversations();
  }, [refreshConversations, resetState, user]);

  useEffect(() => {
    if (!activeConversationId || activeConversationId.startsWith("temp:")) return;
    setLoadingMessages(true);
    const fetchMessages = async () => {
      try {
        const messages = await chatApi.listMessages(activeConversationId, 50);
        setMessagesByConversation((prev) => ({
          ...prev,
          [activeConversationId]: messages,
        }));
        setConversations((prev) =>
          prev.map((conv) =>
            conv.id === activeConversationId ? { ...conv, unreadCount: 0 } : conv,
          ),
        );
        await chatApi.markAsRead(activeConversationId).catch(() => {
          // Do not block UI on read-receipt failure.
        });
      } catch {
        addToast("Failed to load messages", "error");
      } finally {
        setLoadingMessages(false);
      }
    };
    void fetchMessages();
  }, [activeConversationId, addToast]);

  const upsertConversationFromMessage = useCallback(
    (message: ChatMessage, incoming: boolean) => {
      setConversations((prev) => {
        const otherUserId =
          message.senderId === user?.id ? message.receiverId : message.senderId;
        const idx = prev.findIndex((c) => c.id === message.conversationId);
        const unreadIncrement =
          incoming && message.conversationId !== activeConversationId ? 1 : 0;
        if (idx >= 0) {
          const next = [...prev];
          const current = next[idx];
          next[idx] = {
            ...current,
            lastMessage: message,
            unreadCount: (current.unreadCount ?? 0) + unreadIncrement,
          };
          return next;
        }
        const placeholder: ConversationSummary = {
          id: message.conversationId,
          participants: [
            { id: otherUserId },
            ...(user ? [{ id: user.id }] : []),
          ],
          lastMessage: message,
          unreadCount: unreadIncrement,
          createdAt: message.sentAt,
        };
        return [placeholder, ...prev];
      });
    },
    [activeConversationId, user],
  );

  const handleIncomingMessage = useCallback(
    (payload: unknown) => {
      const maybeMessage = payload as Partial<ChatMessage>;
      if (!maybeMessage || !maybeMessage.conversationId || !maybeMessage.senderId) return;
      const message: ChatMessage = {
        id: maybeMessage.id ?? crypto.randomUUID(),
        conversationId: maybeMessage.conversationId,
        senderId: maybeMessage.senderId,
        receiverId: maybeMessage.receiverId ?? "",
        content: maybeMessage.content ?? "",
        sentAt: maybeMessage.sentAt ?? new Date().toISOString(),
        status: maybeMessage.status ?? "DELIVERED",
      };
        setMessagesByConversation((prev) => ({
          ...prev,
          [message.conversationId]: mergeMessages(
            prev[message.conversationId] ?? [],
            message,
          ),
        }));
        const incoming = message.senderId !== user?.id;
        updatePresence(message.senderId, true);
        upsertConversationFromMessage(message, incoming);
      },
      [updatePresence, upsertConversationFromMessage, user?.id],
    );

  const handleAck = useCallback(
    (payload: unknown) => {
      const ack = payload as SendChatMessageResult;
      if (!ack?.persistedMessage) return;
      const message = ack.persistedMessage;
      const tempKey = tempConversationKey(message.receiverId);
      setMessagesByConversation((prev) => {
        const next = { ...prev };
        const cleanedPending =
          next[tempKey]?.filter(
            (msg) =>
              !(
                msg.senderId === message.senderId &&
                msg.content === message.content &&
                msg.status === "PENDING"
              ),
          ) ?? [];
        if (cleanedPending.length) {
          next[tempKey] = cleanedPending;
        } else {
          delete next[tempKey];
        }
        next[message.conversationId] = mergeMessages(
          next[message.conversationId] ?? [],
          message,
        );
        return next;
      });
      updatePresence(message.receiverId, !!ack?.receiverOnline);
      upsertConversationFromMessage(message, false);
      setActiveConversationId((current) =>
        current && current.startsWith("temp:") ? message.conversationId : current,
      );
    },
    [upsertConversationFromMessage, updatePresence],
  );

  const handlePresenceEvent = useCallback((payload: unknown) => {
    const presence = payload as Partial<ChatPresenceEvent>;
    if (!presence?.userId) return;
    setPresenceByUser((prev) => ({
      ...prev,
      [presence.userId]: {
        online: Boolean(presence.online),
        lastActiveAt: presence.lastActiveAt ?? new Date().toISOString(),
      },
    }));
  }, []);

  const { send, status: socketStatus } = useChatClient({
    accessToken,
    onMessage: handleIncomingMessage,
    onAck: handleAck,
    onError: () => {
      addToast("Kết nối chat gặp sự cố", "error");
    },
    onStatusChange: setConnectionStatus,
    onPresence: handlePresenceEvent,
  });

  useEffect(() => {
    setConnectionStatus(socketStatus);
  }, [socketStatus]);

  useEffect(() => {
    if (!user) return;
    if (connectionStatus === "connected") {
      updatePresence(user.id, true);
    } else if (connectionStatus === "error" || connectionStatus === "idle") {
      updatePresence(user.id, false);
    }
  }, [connectionStatus, updatePresence, user]);

  useEffect(() => {
    if (connectionStatus === "connected") return;
    if (!user) return;
    const interval = setInterval(() => {
      void refreshConversations();
      if (activeConversationId && !activeConversationId.startsWith("temp:")) {
        void chatApi
          .listMessages(activeConversationId, 50)
          .then((msgs) =>
            setMessagesByConversation((prev) => ({
              ...prev,
              [activeConversationId]: msgs,
            })),
          )
          .catch(() => undefined);
      }
    }, 8000);
    return () => clearInterval(interval);
  }, [activeConversationId, connectionStatus, refreshConversations, user]);

  const sendMessage = useCallback(
    async (payload: SendChatMessagePayload) => {
      if (!user) throw new Error("Bạn cần đăng nhập để chat");
      const content = payload.content?.trim();
      if (!content) throw new Error("Nội dung tin nhắn không hợp lệ");

      const receiverId = payload.receiverId;
      const conversationKey = payload.conversationId ?? tempConversationKey(receiverId);
      const pendingMessage: ChatMessage = {
        id: `temp-${Date.now()}`,
        conversationId: conversationKey,
        senderId: user.id,
        receiverId,
        content,
        sentAt: new Date().toISOString(),
        status: "PENDING",
      };

      setMessagesByConversation((prev) => ({
        ...prev,
        [conversationKey]: [...(prev[conversationKey] ?? []), pendingMessage],
      }));
      setActiveConversationId(conversationKey);

      try {
        send({
          conversationId: payload.conversationId,
          receiverId,
          content,
        });
      } catch {
        setMessagesByConversation((prev) => ({
          ...prev,
          [conversationKey]: (prev[conversationKey] ?? []).filter(
            (msg) => msg.id !== pendingMessage.id,
          ),
        }));
        throw new Error("Chat connection is not ready");
      }
    },
    [send, user],
  );

  const value = useMemo(
    () => ({
      conversations,
      activeConversationId,
      setActiveConversationId,
      getMessagesForConversation,
      sendMessage,
      connectionStatus,
      loadingConversations,
      loadingMessages,
      refreshConversations,
      getPresenceForUser,
    }),
    [
      activeConversationId,
      connectionStatus,
      conversations,
      getMessagesForConversation,
      loadingConversations,
      loadingMessages,
      refreshConversations,
      sendMessage,
      getPresenceForUser,
    ],
  );

  return <ChatContext.Provider value={value}>{children}</ChatContext.Provider>;
};

export const useChat = () => {
  const ctx = useContext(ChatContext);
  if (!ctx) throw new Error("useChat must be used within ChatProvider");
  return ctx;
};
