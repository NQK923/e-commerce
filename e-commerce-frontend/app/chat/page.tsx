'use client';

import React, { useEffect, useMemo, useRef, useState } from "react";
import { useSearchParams } from "next/navigation";
import {
  ArrowRight,
  Check,
  CheckCheck,
  Loader2,
  MessageSquare,
  MoreVertical,
  Phone,
  Search,
  Send,
  User,
  Video,
} from "lucide-react";
import { useChat } from "@/src/store/chat-store";
import { useAuth } from "@/src/store/auth-store";
import { useRequireAuth } from "@/src/hooks/use-require-auth";
import { ChatMessage, ConversationSummary } from "@/src/types/chat";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { useToast } from "@/src/components/ui/toast-provider";
import { Avatar, AvatarFallback, AvatarImage } from "@/src/components/ui/avatar";

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

  if (date.toDateString() === today.toDateString()) {
    return "Hôm nay";
  } else if (date.toDateString() === yesterday.toDateString()) {
    return "Hôm qua";
  } else {
    return date.toLocaleDateString("vi-VN", { day: "numeric", month: "long" });
  }
};

const getOtherParticipant = (conversation: ConversationSummary | undefined, currentUserId?: string) =>
  conversation?.participants.find((p) => p.id !== currentUserId) ?? null;

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
        active
          ? "bg-emerald-50 shadow-sm ring-1 ring-emerald-200"
          : "hover:bg-zinc-50"
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
              {other?.displayName || other?.id || "Người dùng"}
            </span>
            {isSeller && (
              <span className="rounded bg-orange-100 px-1.5 py-0.5 text-[10px] font-bold text-orange-700 uppercase tracking-wide">
                Seller
              </span>
            )}
          </div>
          {last && (
            <span className="text-[10px] text-zinc-400 whitespace-nowrap">
              {formatTime(last.sentAt)}
            </span>
          )}
        </div>
        <div className="flex items-center justify-between gap-2 mt-0.5">
          <p className={`truncate text-sm ${conversation.unreadCount ? "font-medium text-zinc-900" : "text-zinc-500"}`}>
            {last ? (
              <>
                {last.senderId === currentUserId && "Bạn: "}
                {last.content}
              </>
            ) : (
              <span className="italic text-zinc-400">Chưa có tin nhắn</span>
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
  const scrollContainerRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (activeConversationId) return;
    // Auto-select first conversation if available, otherwise stay on empty or compose
    if (conversations.length > 0 && !composeTarget) {
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

  // Group messages by date
  const groupedMessages = useMemo(() => {
    const groups: { [date: string]: ChatMessage[] } = {};
    messages.forEach((msg) => {
      const dateKey = new Date(msg.sentAt).toDateString();
      if (!groups[dateKey]) groups[dateKey] = [];
      groups[dateKey].push(msg);
    });
    return groups;
  }, [messages]);

  // Scroll to bottom on new messages
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

    if (connectionStatus !== 'connected') {
        addToast("Chưa kết nối đến máy chủ chat. Vui lòng đợi.", "error");
        return;
    }

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

  const otherParticipant = getOtherParticipant(activeConversation, user?.id);
  const isSeller = otherParticipant?.role === "SELLER";

  if (initializing || !isAuthenticated) {
    return (
      <div className="flex min-h-[60vh] flex-col items-center justify-center gap-3 text-zinc-500">
        <Loader2 className="h-8 w-8 animate-spin text-emerald-600" />
        <p className="text-sm font-medium">Đang tải dữ liệu chat...</p>
      </div>
    );
  }

  return (
    <div className="container mx-auto max-w-7xl px-4 py-6 md:py-8 h-[calc(100vh-80px)] min-h-[600px]">
      <div className="grid h-full grid-cols-1 overflow-hidden rounded-2xl border border-zinc-200 bg-white shadow-xl lg:grid-cols-[360px_1fr]">
        
        {/* Sidebar - List Conversations */}
        <div className={`flex flex-col border-r border-zinc-100 bg-white ${activeConversationId ? 'hidden lg:flex' : 'flex'}`}>
          <div className="p-4 border-b border-zinc-50">
             <div className="flex items-center justify-between mb-4">
                <h2 className="text-xl font-bold text-zinc-800">Tin nhắn</h2>
                <div className={`h-2.5 w-2.5 rounded-full ${connectionStatus === 'connected' ? 'bg-emerald-500' : 'bg-red-400'}`} title={connectionStatus} />
             </div>
             
             <div className="relative mb-3">
               <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400" size={16} />
               <Input 
                 className="pl-9 bg-zinc-50 border-zinc-100 focus-visible:ring-emerald-500" 
                 placeholder="Tìm kiếm cuộc trò chuyện..." 
                 value={filter}
                 onChange={(e) => setFilter(e.target.value)}
               />
             </div>

             {/* Temp Input for starting new chat (if needed) */}
             <div className="flex gap-2">
                <Input
                    className="text-xs h-9 bg-zinc-50 border-zinc-100"
                    placeholder="ID người dùng mới..."
                    value={composeTarget}
                    onChange={(e) => {
                        setComposeTarget(e.target.value);
                        if (e.target.value) setActiveConversationId(`temp:${e.target.value}`);
                    }}
                />
             </div>
          </div>

          <div className="flex-1 overflow-y-auto p-3 space-y-1">
            {loadingConversations && filteredConversations.length === 0 ? (
               <div className="flex flex-col items-center justify-center py-8 text-zinc-400">
                  <Loader2 className="h-6 w-6 animate-spin mb-2" />
                  <span className="text-xs">Đang đồng bộ...</span>
               </div>
            ) : filteredConversations.length === 0 ? (
                <div className="text-center py-10 text-zinc-500 text-sm">
                    Không tìm thấy cuộc trò chuyện nào.
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

        {/* Chat Window */}
        <div className={`flex flex-col bg-zinc-50/50 ${!activeConversationId && !composeTarget ? 'hidden lg:flex' : 'flex'}`}>
          {activeConversationId || composeTarget ? (
             <>
               {/* Chat Header */}
               <div className="flex items-center justify-between border-b border-zinc-100 bg-white px-6 py-3 shadow-sm z-10">
                 <div className="flex items-center gap-3">
                   <Button variant="ghost" size="icon" className="lg:hidden -ml-2" onClick={() => setActiveConversationId(null)}>
                      <ArrowRight className="rotate-180" size={20} />
                   </Button>
                   <Avatar className="h-10 w-10 border border-zinc-100">
                     <AvatarImage src={otherParticipant?.avatarUrl} />
                     <AvatarFallback>{otherParticipant?.displayName?.charAt(0) || <User size={20} />}</AvatarFallback>
                   </Avatar>
                   <div>
                     <div className="flex items-center gap-2">
                       <h3 className="font-bold text-zinc-900">
                         {otherParticipant?.displayName || otherParticipant?.id || (composeTarget ? `User ${composeTarget}` : "Unknown")}
                       </h3>
                       {isSeller && (
                         <span className="rounded bg-orange-100 px-1.5 py-0.5 text-[10px] font-bold text-orange-700">SELLER</span>
                       )}
                     </div>
                     <p className="text-xs text-zinc-500 flex items-center gap-1">
                        {connectionStatus === 'connected' ? <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 inline-block"/> : null}
                        {connectionStatus === 'connected' ? 'Đang hoạt động' : 'Ngoại tuyến'}
                     </p>
                   </div>
                 </div>
                 <div className="flex items-center gap-1 text-zinc-400">
                    <Button variant="ghost" size="icon" disabled><Phone size={18} /></Button>
                    <Button variant="ghost" size="icon" disabled><Video size={18} /></Button>
                    <Button variant="ghost" size="icon" disabled><MoreVertical size={18} /></Button>
                 </div>
               </div>

               {/* Messages List */}
               <div 
                  ref={scrollContainerRef}
                  className="flex-1 overflow-y-auto px-4 py-6 bg-[#f0f2f5]" /* Facebook messenger-like bg */
                >
                  {loadingMessages && messages.length === 0 ? (
                      <div className="flex h-full items-center justify-center">
                          <Loader2 className="h-6 w-6 animate-spin text-emerald-600" />
                      </div>
                  ) : (
                    <>
                       {/* Welcome / Encryption Notice */}
                       <div className="mb-6 text-center">
                          <span className="inline-block rounded-lg bg-amber-50 px-3 py-1.5 text-[10px] text-amber-700 border border-amber-100 shadow-sm">
                             🔒 Tin nhắn được bảo mật đầu cuối.
                          </span>
                       </div>

                       {Object.entries(groupedMessages).map(([dateLabel, msgs]) => (
                           <div key={dateLabel}>
                               <div className="sticky top-0 z-0 my-4 flex justify-center">
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

               {/* Input Area */}
               <div className="border-t border-zinc-200 bg-white p-4">
                  <div className="flex items-end gap-2 rounded-2xl border border-zinc-200 bg-zinc-50 px-4 py-2 shadow-sm focus-within:border-emerald-500 focus-within:ring-1 focus-within:ring-emerald-500/50 transition-all">
                     <Input 
                        placeholder="Nhập tin nhắn..." 
                        className="flex-1 border-none bg-transparent p-0 focus-visible:ring-0 min-h-[24px] max-h-[120px] resize-none"
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
                     <Button 
                        onClick={handleSend} 
                        size="icon" 
                        disabled={!draft.trim() || connectionStatus !== 'connected'}
                        className={`h-8 w-8 rounded-full transition-all ${draft.trim() ? 'bg-emerald-600 hover:bg-emerald-700' : 'bg-zinc-200 text-zinc-400'}`}
                     >
                        <Send size={14} />
                     </Button>
                  </div>
                  {connectionStatus !== 'connected' && (
                      <p className="mt-2 text-center text-xs text-red-500">
                          Mất kết nối máy chủ. Đang thử lại...
                      </p>
                  )}
               </div>
             </>
          ) : (
            /* Empty State */
            <div className="flex h-full flex-col items-center justify-center p-8 text-center bg-white">
                <div className="mb-6 flex h-24 w-24 items-center justify-center rounded-full bg-emerald-50 text-emerald-200">
                    <MessageSquare size={48} className="text-emerald-500" />
                </div>
                <h3 className="text-xl font-bold text-zinc-900">Chào mừng đến với E-Com Chat</h3>
                <p className="mt-2 max-w-sm text-sm text-zinc-500">
                    Chọn một cuộc hội thoại từ danh sách bên trái hoặc bắt đầu cuộc trò chuyện mới để kết nối với người bán.
                </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
