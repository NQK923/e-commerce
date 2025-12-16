'use client';

import React, { useEffect, useMemo, useRef, useState } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import {
  ArrowLeft,
  Check,
  CheckCheck,
  Loader2,
  MessageCircle,
  MessageSquare,
  MoreVertical,
  Phone,
  Search,
  Send,
  User,
  Video,
  X,
} from "lucide-react";
import { useChat } from "@/src/store/chat-store";
import { useAuth } from "@/src/store/auth-store";
import { ChatMessage, ConversationSummary } from "@/src/types/chat";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { useToast } from "@/src/components/ui/toast-provider";
import { Avatar, AvatarFallback, AvatarImage } from "@/src/components/ui/avatar";

type ChatWidgetProps = {
  fullPage?: boolean;
  initialTargetUserId?: string | null;
};

const formatTime = (value?: string) => {
  if (!value) return "";
  const date = new Date(value);
  return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
};

const formatDateGroup = (dateStr: string) => {
  const date = new Date(dateStr);
  const today = new Date();
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);

  if (date.toDateString() === today.toDateString()) return "Today";
  if (date.toDateString() === yesterday.toDateString()) return "Yesterday";
  return date.toLocaleDateString("en-US", { day: "numeric", month: "long" });
};

const getOtherParticipant = (
  conversation: ConversationSummary | undefined,
  currentUserId?: string,
) => conversation?.participants.find((p) => p.id !== currentUserId) ?? null;

const MessageBubble: React.FC<{
  message: ChatMessage;
  isMine: boolean;
}> = ({ message, isMine }) => {
  return (
    <div className={`flex w-full ${isMine ? "justify-end" : "justify-start"} mb-2`}>
      <div
        className={`relative max-w-[75%] px-4 py-2 text-sm shadow-sm ${
          isMine
            ? "bg-emerald-600 text-white rounded-2xl rounded-tr-sm"
            : "bg-white text-zinc-900 border border-zinc-100 rounded-2xl rounded-tl-sm"
        }`}
      >
        <p className="whitespace-pre-wrap break-words leading-relaxed">{message.content}</p>
        <div
          className={`mt-1 flex items-center justify-end gap-1 text-[10px] ${
            isMine ? "text-emerald-100" : "text-zinc-400"
          }`}
        >
          <span>{formatTime(message.sentAt)}</span>
          {isMine && (
            <span className="ml-1">
              {message.status === "READ" ? (
                <CheckCheck size={14} className="text-blue-200" />
              ) : (
                <Check size={14} className="text-emerald-200" />
              )}
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
  const isSeller = other?.role === "SELLER";

  return (
    <button
      onClick={onSelect}
      className={`group flex w-full items-center gap-3 rounded-xl p-3 text-left transition-all ${
        active ? "bg-emerald-50 shadow-sm ring-1 ring-emerald-200" : "hover:bg-zinc-50"
      }`}
    >
      <div className="relative">
        <Avatar className="h-12 w-12 border border-zinc-100">
          <AvatarImage src={other?.avatarUrl} />
          <AvatarFallback className={active ? "bg-emerald-200 text-emerald-800" : "bg-zinc-100 text-zinc-600"}>
            {other?.displayName?.charAt(0).toUpperCase() || <User size={20} />}
          </AvatarFallback>
        </Avatar>
        {active && (
          <span className="absolute bottom-0 right-0 h-3 w-3 rounded-full bg-emerald-500 ring-2 ring-white" />
        )}
      </div>

      <div className="flex-1 overflow-hidden">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="font-semibold text-zinc-900 truncate">
              {other?.displayName || other?.id || "Unknown"}
            </span>
            {isSeller && (
              <span className="rounded bg-orange-100 px-1.5 py-0.5 text-[10px] font-bold text-orange-700 uppercase tracking-wide">
                Seller
              </span>
            )}
          </div>
          {last && (
            <span className="text-[10px] text-zinc-400 whitespace-nowrap">{formatTime(last.sentAt)}</span>
          )}
        </div>
        <div className="mt-0.5 flex items-center justify-between gap-2">
          <p className={`truncate text-sm ${conversation.unreadCount ? "font-medium text-zinc-900" : "text-zinc-500"}`}>
            {last ? (
              <>
                {last.senderId === currentUserId && "You: "}
                {last.content}
              </>
            ) : (
              <span className="italic text-zinc-400">No messages yet</span>
            )}
          </p>
          {conversation.unreadCount ? (
            <span className="flex h-5 w-5 min-w-[1.25rem] items-center justify-center rounded-full bg-emerald-600 text-[10px] font-bold text-white shadow-sm">
              {conversation.unreadCount}
            </span>
          ) : null}
        </div>
      </div>
    </button>
  );
};

export const ChatWidget: React.FC<ChatWidgetProps> = ({ fullPage = false, initialTargetUserId }) => {
  const { user, initializing } = useAuth();
  const isAuthenticated = Boolean(user);
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const { addToast } = useToast();
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
  const [composeTarget, setComposeTarget] = useState(initialTargetUserId ?? "");
  const [open, setOpen] = useState(fullPage);

  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    setComposeTarget(initialTargetUserId ?? "");
  }, [initialTargetUserId]);

  useEffect(() => {
    if (fullPage) setOpen(true);
  }, [fullPage]);

  useEffect(() => {
    if (!isAuthenticated) return;
    if (activeConversationId) return;
    if (conversations.length > 0 && !composeTarget) {
      setActiveConversationId(conversations[0].id);
    } else if (composeTarget) {
      setActiveConversationId(`temp:${composeTarget}`);
    }
  }, [activeConversationId, composeTarget, conversations, isAuthenticated, setActiveConversationId]);

  const activeConversation = useMemo(
    () => conversations.find((c) => c.id === activeConversationId),
    [activeConversationId, conversations],
  );

  const derivedConversationKey = activeConversationId ?? (composeTarget ? `temp:${composeTarget}` : null);
  const messages = getMessagesForConversation(derivedConversationKey);

  const groupedMessages = useMemo(() => {
    const groups: Record<string, ChatMessage[]> = {};
    messages.forEach((msg) => {
      const dateKey = new Date(msg.sentAt).toDateString();
      if (!groups[dateKey]) groups[dateKey] = [];
      groups[dateKey].push(msg);
    });
    return Object.entries(groups).sort(
      ([a], [b]) => new Date(a).getTime() - new Date(b).getTime(),
    );
  }, [messages]);

  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [messages.length, loadingMessages, activeConversationId]);

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

    if (connectionStatus !== "connected") {
      addToast("Chat server is not connected yet. Please try again.", "error");
      return;
    }

    const receiverId = getOtherParticipant(activeConversation, user?.id)?.id || composeTarget || undefined;
    if (!receiverId) {
      addToast("Pick someone to chat with first.", "error");
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
      const message = error instanceof Error ? error.message : "Failed to send message";
      addToast(message, "error");
    }
  };

  const otherParticipant = getOtherParticipant(activeConversation, user?.id);
  const isSeller = otherParticipant?.role === "SELLER";
  const unreadTotal = useMemo(
    () =>
      conversations.reduce((sum, conv) => sum + (conv.unreadCount ?? 0), 0),
    [conversations],
  );

  const loginHref = useMemo(() => {
    const next = pathname + (searchParams?.toString() ? `?${searchParams.toString()}` : "");
    return `/login?next=${encodeURIComponent(next)}`;
  }, [pathname, searchParams]);

  const ChatSurface = (
    <div className="flex h-full flex-col bg-white">
      <div className="relative overflow-hidden border-b border-transparent bg-gradient-to-r from-emerald-600 via-emerald-500 to-teal-500 px-4 py-3 text-white shadow-sm">
        <div className="absolute inset-0 opacity-20 bg-[radial-gradient(circle_at_20%_-10%,white,transparent_35%),radial-gradient(circle_at_80%_0,white,transparent_25%)]" />
        <div className="relative flex items-center justify-between">
          <div className="flex items-center gap-3">
            {!fullPage && (
              <Button
                variant="ghost"
                size="icon"
                className="text-white hover:text-white/90 lg:hidden"
                onClick={() => {
                  setActiveConversationId(null);
                  setOpen(false);
                }}
              >
                <X size={18} />
              </Button>
            )}
            <div>
              <p className="text-[11px] uppercase tracking-[0.12em] text-white/80">Message Center</p>
              <div className="flex items-center gap-2">
                <h3 className="text-lg font-semibold leading-tight">Chat Assistant</h3>
                <span
                  className={`flex items-center gap-1 rounded-full px-2 py-0.5 text-[11px] font-semibold ${
                    connectionStatus === "connected" ? "bg-white/20 text-white" : "bg-black/20 text-white/80"
                  }`}
                >
                  <span className={`h-2 w-2 rounded-full ${
                    connectionStatus === "connected" ? "bg-lime-300" : "bg-orange-200"
                  }`} />
                  {connectionStatus === "connected" ? "Live" : "Connecting"}
                </span>
              </div>
            </div>
          </div>
          {!fullPage && (
            <Button variant="ghost" size="icon" className="text-white hover:text-white/90" onClick={() => setOpen(false)}>
              <X size={18} />
            </Button>
          )}
        </div>
      </div>

      {initializing ? (
        <div className="flex flex-1 flex-col items-center justify-center gap-3 px-6 text-center text-zinc-500">
          <Loader2 className="h-6 w-6 animate-spin text-emerald-600" />
          <p className="text-sm font-medium">Loading chat...</p>
        </div>
      ) : !isAuthenticated ? (
        <div className="flex flex-1 flex-col items-center justify-center gap-4 px-6 text-center">
          <MessageSquare className="h-10 w-10 text-emerald-500" />
          <div className="space-y-1">
            <p className="text-base font-semibold text-zinc-900">Sign in to start chatting</p>
            <p className="text-sm text-zinc-500">
              Chat with sellers and track your conversations across the site.
            </p>
          </div>
          <Button className="w-full" onClick={() => router.push(loginHref)}>
            Go to login
          </Button>
        </div>
      ) : (
        <div className="grid h-full grid-cols-1 lg:grid-cols-[320px_1fr]">
          <div
            className={`flex flex-col border-r border-zinc-100 bg-gradient-to-b from-white via-zinc-50 to-white ${
              activeConversationId ? "hidden lg:flex" : "flex"
            }`}
          >
            <div className="space-y-3 border-b border-zinc-100/60 p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-[11px] uppercase tracking-[0.12em] text-emerald-700/80">Inbox</p>
                  <h4 className="text-lg font-semibold text-zinc-900">Conversations</h4>
                </div>
                <span className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700 shadow-sm">
                  {unreadTotal} unread
                </span>
              </div>

              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400" size={16} />
                <Input
                  className="pl-10 bg-white/80 border-zinc-200 focus-visible:ring-emerald-500"
                  placeholder="Search conversations..."
                  value={filter}
                  onChange={(e) => setFilter(e.target.value)}
                />
              </div>

              <div className="rounded-xl border border-dashed border-emerald-200 bg-emerald-50/60 p-3">
                <p className="mb-2 text-xs font-semibold text-emerald-800">Start a new chat</p>
                <Input
                  className="h-9 bg-white text-xs"
                  placeholder="Enter user ID..."
                  value={composeTarget}
                  onChange={(e) => {
                    setComposeTarget(e.target.value);
                    if (e.target.value) setActiveConversationId(`temp:${e.target.value}`);
                  }}
                />
              </div>
            </div>

            <div className="flex-1 space-y-1 overflow-y-auto p-3">
              {loadingConversations && filteredConversations.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-8 text-zinc-400">
                  <Loader2 className="mb-2 h-6 w-6 animate-spin" />
                  <span className="text-xs">Loading conversations...</span>
                </div>
              ) : filteredConversations.length === 0 ? (
                <div className="rounded-xl border border-dashed border-zinc-200 bg-white/80 p-6 text-center text-sm text-zinc-600 shadow-sm">
                  <p className="font-semibold text-zinc-800">No conversations yet</p>
                  <p className="mt-1 text-xs text-zinc-500">Start by entering a user ID above.</p>
                </div>
              ) : (
                filteredConversations.map((conv) => (
                  <ConversationItem
                    key={conv.id}
                    conversation={conv}
                    active={conv.id === activeConversationId}
                    currentUserId={user?.id}
                    onSelect={() => {
                      setComposeTarget("");
                      setActiveConversationId(conv.id);
                    }}
                  />
                ))
              )}
            </div>
          </div>

          <div
            className={`flex flex-col bg-gradient-to-b from-[#f6f9f7] via-[#f2f5f9] to-[#edf3f1] ${
              !activeConversationId && !composeTarget ? "hidden lg:flex" : "flex"
            }`}
          >
            {activeConversationId || composeTarget ? (
              <>
                <div className="z-10 flex items-center justify-between border-b border-white/70 bg-white/90 px-4 py-3 backdrop-blur">
                  <div className="flex items-center gap-3">
                    <Button
                      variant="ghost"
                      size="icon"
                      className="lg:hidden -ml-2"
                      onClick={() => setActiveConversationId(null)}
                    >
                      <ArrowLeft size={18} />
                    </Button>
                    <Avatar className="h-10 w-10 border border-zinc-100">
                      <AvatarImage src={otherParticipant?.avatarUrl} />
                      <AvatarFallback>{otherParticipant?.displayName?.charAt(0) || <User size={20} />}</AvatarFallback>
                    </Avatar>
                    <div>
                      <div className="flex items-center gap-2">
                        <h3 className="font-bold text-zinc-900">
                          {otherParticipant?.displayName ||
                            otherParticipant?.id ||
                            (composeTarget ? `User ${composeTarget}` : "Unknown")}
                        </h3>
                        {isSeller && (
                          <span className="rounded bg-orange-100 px-1.5 py-0.5 text-[10px] font-bold text-orange-700">
                            SELLER
                          </span>
                        )}
                      </div>
                      <p className="flex items-center gap-1 text-xs text-zinc-500">
                        {connectionStatus === "connected" ? (
                          <span className="inline-block h-1.5 w-1.5 rounded-full bg-emerald-500" />
                        ) : null}
                        {connectionStatus === "connected" ? "Online" : "Offline"}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-1 text-zinc-400">
                    <Button variant="ghost" size="icon" disabled>
                      <Phone size={16} />
                    </Button>
                    <Button variant="ghost" size="icon" disabled>
                      <Video size={16} />
                    </Button>
                    <Button variant="ghost" size="icon" disabled>
                      <MoreVertical size={16} />
                    </Button>
                  </div>
                </div>

                <div className="flex-1 overflow-y-auto px-4 py-4">
                  {loadingMessages && messages.length === 0 ? (
                    <div className="flex h-full items-center justify-center">
                      <Loader2 className="h-6 w-6 animate-spin text-emerald-600" />
                    </div>
                  ) : (
                    <>
                      <div className="mb-4 flex flex-wrap items-center justify-between gap-2">
                        <span className="inline-flex items-center gap-2 rounded-full border border-emerald-100 bg-white/70 px-3 py-1 text-[11px] font-medium text-emerald-700 shadow-sm backdrop-blur">
                          <span className={`h-2 w-2 rounded-full ${
                            connectionStatus === "connected" ? "bg-emerald-500" : "bg-orange-400"
                          }`} />
                          Secure conversation channel
                        </span>
                        <span className="rounded-full bg-emerald-50 px-2.5 py-1 text-[10px] font-semibold uppercase tracking-wide text-emerald-700 shadow-sm">
                          End-to-end focus
                        </span>
                      </div>

                      {groupedMessages.map(([dateLabel, msgs]) => (
                        <div key={dateLabel}>
                          <div className="sticky top-0 z-0 my-3 flex justify-center">
                            <span className="rounded-full bg-zinc-200/80 px-3 py-1 text-xs font-medium text-zinc-600 shadow-sm backdrop-blur-sm">
                              {formatDateGroup(dateLabel)}
                            </span>
                          </div>
                          {msgs.map((msg) => (
                            <MessageBubble key={msg.id} message={msg} isMine={msg.senderId === user?.id} />
                          ))}
                        </div>
                      ))}
                      <div ref={messagesEndRef} className="h-1" />
                    </>
                  )}
                </div>

                <div className="border-t border-white/70 bg-white/90 p-3 backdrop-blur">
                  <div className="flex items-end gap-2 rounded-2xl border border-zinc-200 bg-white px-3 py-2 shadow-sm focus-within:border-emerald-500 focus-within:ring-1 focus-within:ring-emerald-500/50 transition-all">
                    <Input
                      placeholder="Type a message..."
                      className="min-h-[24px] flex-1 resize-none border-none bg-transparent p-0 focus-visible:ring-0"
                      value={draft}
                      onChange={(e) => setDraft(e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === "Enter" && !e.shiftKey) {
                          e.preventDefault();
                          void handleSend();
                        }
                      }}
                      autoComplete="off"
                    />
                    <div className="flex items-center gap-2">
                      <span className="hidden text-[11px] font-medium text-zinc-400 sm:block">
                        Enter to send
                      </span>
                      <Button
                        onClick={handleSend}
                        size="icon"
                        disabled={!draft.trim() || connectionStatus !== "connected"}
                        className={`h-9 w-9 rounded-full shadow-sm transition-all ${
                          draft.trim()
                            ? "bg-emerald-600 hover:bg-emerald-700"
                            : "bg-zinc-200 text-zinc-400"
                        }`}
                      >
                        <Send size={15} />
                      </Button>
                    </div>
                  </div>
                  {connectionStatus !== "connected" && (
                    <p className="mt-2 text-center text-xs text-red-500">
                      Reconnecting to chat service...
                    </p>
                  )}
                </div>
              </>
            ) : (
              <div className="flex h-full flex-col items-center justify-center bg-white p-8 text-center">
                <div className="mb-6 flex h-20 w-20 items-center justify-center rounded-full bg-emerald-50 text-emerald-200">
                  <MessageSquare size={40} className="text-emerald-500" />
                </div>
                <h3 className="text-lg font-bold text-zinc-900">Start a conversation</h3>
                <p className="mt-2 max-w-sm text-sm text-zinc-500">
                  Choose a conversation on the left or enter a user ID to begin chatting.
                </p>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );

  if (fullPage) {
    return (
      <div className="container mx-auto max-w-6xl px-4 py-6 md:py-8">
        <div className="h-[calc(100vh-160px)] min-h-[540px] overflow-hidden rounded-2xl border border-zinc-200 bg-white shadow-xl">
          {ChatSurface}
        </div>
      </div>
    );
  }

  return (
    <div className="fixed bottom-5 right-4 z-50 flex flex-col items-end gap-3">
      <div className="flex items-center gap-2">
        {connectionStatus === "connecting" && (
          <span className="rounded-full bg-amber-100 px-3 py-1 text-xs text-amber-700 shadow-sm">
            Connecting...
          </span>
        )}
      </div>
      <Button
        size="icon"
        className="relative h-12 w-12 rounded-full bg-emerald-600 shadow-lg transition hover:bg-emerald-700"
        onClick={() => setOpen((prev) => !prev)}
      >
        <MessageCircle className="h-6 w-6 text-white" />
        {unreadTotal > 0 && (
          <span className="absolute -right-1 -top-1 flex h-5 w-5 items-center justify-center rounded-full bg-white text-[11px] font-semibold text-emerald-700 shadow">
            {unreadTotal}
          </span>
        )}
      </Button>
      {open && (
        <div className="w-[360px] sm:w-[420px] h-[560px] overflow-hidden rounded-2xl border border-zinc-200 bg-white shadow-2xl">
          {ChatSurface}
        </div>
      )}
    </div>
  );
};
