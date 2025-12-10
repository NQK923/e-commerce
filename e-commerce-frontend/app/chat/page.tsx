'use client';

import React, { useEffect, useMemo, useRef, useState } from "react";
import { useSearchParams } from "next/navigation";
import {
  ArrowRight,
  CheckCheck,
  Loader2,
  MessagesSquare,
  Radio,
  Send,
  UserCircle,
} from "lucide-react";
import { useChat } from "@/src/store/chat-store";
import { useAuth } from "@/src/store/auth-store";
import { useRequireAuth } from "@/src/hooks/use-require-auth";
import { ChatMessage, ConversationSummary } from "@/src/types/chat";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { useToast } from "@/src/components/ui/toast-provider";

const formatTime = (value?: string) => {
  if (!value) return "";
  const date = new Date(value);
  return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
};

const getOtherParticipant = (conversation: ConversationSummary | undefined, currentUserId?: string) =>
  conversation?.participants.find((p) => p.id !== currentUserId) ?? null;

const MessageBubble: React.FC<{
  message: ChatMessage;
  isMine: boolean;
}> = ({ message, isMine }) => {
  return (
    <div className={`flex ${isMine ? "justify-end" : "justify-start"}`}>
      <div
        className={`max-w-[75%] rounded-2xl px-4 py-2 text-sm shadow-sm ${
          isMine
            ? "bg-emerald-600 text-white rounded-br-md"
            : "bg-white text-zinc-900 border border-zinc-100 rounded-bl-md"
        }`}
      >
        <p className="whitespace-pre-wrap break-words">{message.content}</p>
        <div
          className={`mt-1 flex items-center gap-1 text-[11px] ${
            isMine ? "text-emerald-50" : "text-zinc-500"
          }`}
        >
          <span>{formatTime(message.sentAt)}</span>
          {isMine && message.status && (
            <span className="flex items-center gap-0.5">
              <CheckCheck size={14} /> {message.status.toLowerCase()}
            </span>
          )}
        </div>
      </div>
    </div>
  );
};

const ConversationItem: React.FC<{
  conversation: ConversationSummary;
  active: boolean;
  onSelect: () => void;
  currentUserId?: string;
}> = ({ conversation, active, onSelect, currentUserId }) => {
  const other = getOtherParticipant(conversation, currentUserId);
  const last = conversation.lastMessage;
  return (
    <button
      onClick={onSelect}
      className={`flex w-full items-center gap-3 rounded-xl border px-3 py-3 text-left transition ${
        active
          ? "border-emerald-500/60 bg-emerald-50/70 shadow-sm"
          : "border-transparent bg-white hover:border-emerald-100 hover:bg-emerald-50/40"
      }`}
    >
      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-emerald-100 text-emerald-700">
        <UserCircle size={22} />
      </div>
      <div className="flex-1 overflow-hidden">
        <div className="flex items-center justify-between">
          <p className="font-semibold text-sm text-zinc-900 truncate">
            {other?.displayName || other?.id || "Người dùng"}
          </p>
          {conversation.unreadCount ? (
            <span className="rounded-full bg-emerald-600 px-2 py-0.5 text-[11px] font-semibold text-white">
              {conversation.unreadCount}
            </span>
          ) : null}
        </div>
        <p className="text-xs text-zinc-500 truncate">
          {last ? `${last.senderId === currentUserId ? "Bạn: " : ""}${last.content}` : "Chưa có tin nhắn"}
        </p>
      </div>
    </button>
  );
};

export default function ChatPage() {
  const { user } = useAuth();
  const searchParams = useSearchParams();
  const { addToast } = useToast();
  const { isAuthenticated, initializing } = useRequireAuth();
  const {
    conversations,
    activeConversationId,
    setActiveConversationId,
    getMessagesForConversation,
    sendMessage,
    connectionStatus,
    loadingConversations,
    loadingMessages,
  } = useChat();

  const [filter, setFilter] = useState("");
  const [draft, setDraft] = useState("");
  const [composeTarget, setComposeTarget] = useState(() => searchParams.get("userId") ?? "");
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (activeConversationId) return;
    if (conversations.length > 0) {
      setActiveConversationId(conversations[0].id);
    } else if (composeTarget) {
      setActiveConversationId(`temp:${composeTarget}`);
    }
  }, [activeConversationId, composeTarget, conversations, setActiveConversationId]);

  const activeConversation = useMemo(
    () => conversations.find((c) => c.id === activeConversationId),
    [activeConversationId, conversations],
  );

  const derivedConversationKey = activeConversationId ?? (composeTarget ? `temp:${composeTarget}` : null);
  const messages = getMessagesForConversation(derivedConversationKey);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages.length, loadingMessages]);

  const filteredConversations = conversations.filter((conversation) => {
    const other = getOtherParticipant(conversation, user?.id);
    if (!filter.trim()) return true;
    return (
      other?.displayName?.toLowerCase().includes(filter.toLowerCase()) ||
      other?.id?.toLowerCase().includes(filter.toLowerCase())
    );
  });

  const handleSend = async () => {
    const content = draft.trim();
    if (!content) return;

    const receiverId =
      getOtherParticipant(activeConversation, user?.id)?.id || composeTarget || undefined;
    if (!receiverId) {
      addToast("Chọn người nhận trước khi gửi", "error");
      return;
    }

    try {
      await sendMessage({
        conversationId: activeConversation?.id,
        receiverId,
        content,
      });
      setDraft("");
    } catch (error) {
      const message = error instanceof Error ? error.message : "Gửi tin nhắn thất bại";
      addToast(message, "error");
    }
  };

  if (initializing || !isAuthenticated) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center gap-2 text-sm text-zinc-600">
        <Loader2 className="h-5 w-5 animate-spin" />
        Đang tải...
      </div>
    );
  }

  return (
    <div className="mx-auto flex max-w-6xl flex-col gap-6 px-4 py-8">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm uppercase tracking-[0.2em] text-emerald-700">Chat trực tiếp</p>
          <h1 className="text-3xl font-bold text-zinc-900">Tin nhắn 1-1</h1>
          <p className="text-sm text-zinc-600">
            Kết nối giữa người mua và người bán theo thời gian thực.
          </p>
        </div>
        <div
          className={`inline-flex items-center gap-2 rounded-full px-3 py-1 text-xs font-semibold ${
            connectionStatus === "connected"
              ? "bg-emerald-100 text-emerald-700"
              : connectionStatus === "connecting"
                ? "bg-amber-100 text-amber-700"
                : "bg-rose-100 text-rose-700"
          }`}
        >
          <Radio className={`h-4 w-4 ${connectionStatus === "connected" ? "animate-pulse" : ""}`} />
          {connectionStatus === "connected"
            ? "Đã kết nối"
            : connectionStatus === "connecting"
              ? "Đang kết nối..."
              : "Mất kết nối"}
        </div>
      </div>

      <div className="grid h-[70vh] grid-cols-1 gap-4 rounded-3xl border border-zinc-200 bg-white shadow-md shadow-emerald-50/50 md:grid-cols-[320px_1fr]">
        <div className="flex flex-col border-b md:border-b-0 md:border-r border-zinc-100 bg-gradient-to-b from-emerald-50/70 to-white rounded-t-3xl md:rounded-tr-none md:rounded-l-3xl">
          <div className="p-4 pb-2">
            <div className="flex items-center gap-2 text-sm font-semibold text-emerald-900">
              <MessagesSquare size={16} />
              Hội thoại
            </div>
            <div className="mt-3 space-y-3">
              <Input
                placeholder="Tìm người dùng..."
                value={filter}
                onChange={(e) => setFilter(e.target.value)}
              />
              <div className="flex gap-2">
                <Input
                  placeholder="Nhập ID người dùng để bắt đầu"
                  value={composeTarget}
                  onChange={(e) => {
                    setComposeTarget(e.target.value);
                    if (e.target.value) {
                      setActiveConversationId(`temp:${e.target.value}`);
                    }
                  }}
                />
                <Button
                  variant="secondary"
                  onClick={() => {
                    if (!composeTarget.trim()) {
                      addToast("Nhập ID người nhận", "error");
                      return;
                    }
                    setActiveConversationId(`temp:${composeTarget.trim()}`);
                  }}
                >
                  <ArrowRight size={16} />
                </Button>
              </div>
            </div>
          </div>
          <div className="flex-1 overflow-y-auto space-y-2 px-4 pb-4">
            {loadingConversations && (
              <div className="flex items-center gap-2 rounded-xl border border-emerald-100 bg-white p-3 text-sm text-zinc-600 shadow-sm">
                <Loader2 className="h-4 w-4 animate-spin text-emerald-600" />
                Đang tải danh sách...
              </div>
            )}
            {!loadingConversations && filteredConversations.length === 0 ? (
              <div className="rounded-xl border border-dashed border-zinc-200 bg-white p-4 text-sm text-zinc-500">
                Chưa có hội thoại nào
              </div>
            ) : (
              filteredConversations.map((conversation) => (
                <ConversationItem
                  key={conversation.id}
                  conversation={conversation}
                  active={conversation.id === activeConversationId}
                  currentUserId={user?.id}
                  onSelect={() => {
                    setComposeTarget("");
                    setActiveConversationId(conversation.id);
                  }}
                />
              ))
            )}
          </div>
        </div>

        <div className="flex flex-col rounded-b-3xl md:rounded-bl-none md:rounded-r-3xl">
          <div className="flex items-center justify-between border-b border-zinc-100 px-4 py-3">
            <div>
              <p className="text-sm text-zinc-500">Đang trò chuyện với</p>
              <p className="text-lg font-semibold text-zinc-900">
                {getOtherParticipant(activeConversation, user?.id)?.displayName ||
                  getOtherParticipant(activeConversation, user?.id)?.id ||
                  (composeTarget ? `Người dùng ${composeTarget}` : "Chưa chọn người nhận")}
              </p>
            </div>
            <div className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">
              {connectionStatus === "connected" ? "Online" : "Đang xử lý..."}
            </div>
          </div>

          <div className="flex-1 space-y-2 overflow-y-auto bg-emerald-50/40 px-4 py-4">
            {loadingMessages ? (
              <div className="flex h-full items-center justify-center gap-2 text-sm text-zinc-600">
                <Loader2 className="h-4 w-4 animate-spin text-emerald-600" />
                Đang tải tin nhắn...
              </div>
            ) : messages.length === 0 ? (
              <div className="flex h-full items-center justify-center text-sm text-zinc-500">
                Hãy gửi tin nhắn đầu tiên của bạn.
              </div>
            ) : (
              messages.map((message) => (
                <MessageBubble
                  key={message.id}
                  message={message}
                  isMine={message.senderId === user?.id}
                />
              ))
            )}
            <div ref={messagesEndRef} />
          </div>

          <div className="border-t border-zinc-100 bg-white px-4 py-3">
            <div className="flex items-center gap-3 rounded-2xl border border-zinc-200 bg-zinc-50 px-3 py-2 shadow-inner">
              <Input
                placeholder="Nhập tin nhắn..."
                value={draft}
                onChange={(e) => setDraft(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" && !e.shiftKey) {
                    e.preventDefault();
                    void handleSend();
                  }
                }}
                className="flex-1 border-none bg-transparent focus-visible:ring-0"
              />
              <Button onClick={handleSend} disabled={!draft.trim()}>
                <Send size={16} className="mr-2" />
                Gửi
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
