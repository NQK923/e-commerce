'use client';

import React, { useEffect, useMemo, useRef, useState } from "react";
import { usePathname, useRouter } from "next/navigation";
import {
  ArrowLeft,
  Check,
  CheckCheck,
  Image as ImageIcon,
  Loader2,
  MessageCircle,
  MessageSquareMore,
  Minimize2,
  MoreHorizontal,
  Phone,
  Search,
  Send,
  Smile,
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
import { uploadToBucket } from "@/src/lib/storage";
import { config } from "@/src/config/env";
import { userApi } from "@/src/api/userApi";

type ChatWidgetProps = {
  fullPage?: boolean;
  initialTargetUserId?: string | null;
};

// --- Helpers ---

const formatTimeShort = (dateStr: string) => {
  const date = new Date(dateStr);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffDays = diffMs / (1000 * 60 * 60 * 24);

  if (diffDays < 1) {
    return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  } else if (diffDays < 7) {
    return date.toLocaleDateString([], { weekday: "short" });
  } else {
    return date.toLocaleDateString([], { day: "numeric", month: "short" });
  }
};

const getOtherParticipant = (
  conversation: ConversationSummary | undefined,
  currentUserId?: string,
) => conversation?.participants.find((p) => p.id !== currentUserId) ?? null;

const formatShopFallback = (id?: string) => {
  if (!id) return "Shop";
  const suffix = id.length > 4 ? id.slice(-4).toUpperCase() : id.toUpperCase();
  return `Shop ${suffix}`;
};

const resolveParticipantName = (
  participant: ConversationSummary["participants"][number] | null,
  fallbackId?: string,
) => {
  if (!participant) return formatShopFallback(fallbackId);
  return (
    participant.storeName ||
    participant.displayName ||
    formatShopFallback(participant.id || fallbackId)
  );
};

type ParticipantProfile = {
  displayName?: string;
  avatarUrl?: string;
};

const isImageContent = (content: string): boolean => {
  const lowered = content.toLowerCase();
  const imageExts = [".jpg", ".jpeg", ".png", ".gif", ".webp", ".avif", ".svg"];
  const looksLikeUrl = lowered.startsWith("http://") || lowered.startsWith("https://");
  return looksLikeUrl && imageExts.some((ext) => lowered.includes(ext));
};

// --- Sub-components ---

type MessageBubbleProps = {
  message: ChatMessage;
  isMine: boolean;
  isFirstInGroup: boolean; // Is this the first message in a series from this sender?
  isLastInGroup: boolean;  // Is this the last message in a series from this sender?
  showAvatar: boolean;
  senderAvatar?: string;
  senderName?: string;
};

const MessageBubble: React.FC<MessageBubbleProps> = ({ 
  message, isMine, isFirstInGroup, isLastInGroup, showAvatar, senderAvatar, senderName
}) => {
  // Define border radius for each corner
  const getBorderRadius = () => {
    if (isMine) {
      // My messages are on the right
      const topLeft = isFirstInGroup ? '18px' : '4px';
      const topRight = '18px';
      const bottomRight = '18px';
      const bottomLeft = isLastInGroup ? '18px' : '4px';
      return `${topLeft} ${topRight} ${bottomRight} ${bottomLeft}`;
    } else {
      // Their messages are on the left
      const topLeft = '18px';
      const topRight = isFirstInGroup ? '18px' : '4px';
      const bottomRight = isLastInGroup ? '18px' : '4px';
      const bottomLeft = '18px';
      return `${topLeft} ${topRight} ${bottomRight} ${bottomLeft}`;
    }
  };

  const isImage = isImageContent(message.content);

  const bubbleClass = isImage
    ? "bg-transparent shadow-none p-0"
    : isMine
        ? "bg-linear-to-br from-emerald-500 to-emerald-600 text-white px-3 py-2 shadow-sm"
        : "bg-white text-zinc-800 border border-zinc-100 px-3 py-2 shadow-sm";

  return (
    <div className={`flex w-full ${isMine ? "justify-end" : "justify-start"} ${isLastInGroup ? 'mb-3' : 'mb-0.5'} group`}>
      {/* Avatar column (only for incoming) */}
      {!isMine && (
        <div className="flex flex-col justify-end mr-2 w-8 shrink-0">
          {showAvatar ? (
            <Avatar className="h-8 w-8 ring-2 ring-white shadow-sm">
              <AvatarImage src={senderAvatar} />
              <AvatarFallback className="text-[10px] bg-emerald-100 text-emerald-700 font-bold">
                {senderName?.charAt(0) || <User size={12} />}
              </AvatarFallback>
            </Avatar>
          ) : (
            <div className="w-8" />
          )}
        </div>
      )}
      
      <div
        className={`relative text-[15px] leading-relaxed wrap-break-word transition-all duration-200 min-w-[30px] ${bubbleClass}`}
        style={{ borderRadius: getBorderRadius(), maxWidth: isImage ? "320px" : "100%" }}
      >
        {isImage ? (
          <a href={message.content} target="_blank" rel="noreferrer" className="block overflow-hidden rounded-xl">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img src={message.content} alt="Chat attachment" className="max-h-72 w-full object-cover" />
          </a>
        ) : (
          <p>{message.content}</p>
        )}
        {isMine && isLastInGroup && ( // Show status only for my last message in a group
          <div className="mt-1 text-[10px] flex justify-end items-center gap-1">
            {message.status === 'READ' ? <CheckCheck size={12} className="text-emerald-200" /> : <Check size={12} className="text-emerald-200" />}
          </div>
        )}
      </div>
      {/* Optional: Timestamp for the bubble */}
      <span className="text-[10px] text-zinc-400 mt-1 ml-1 self-end opacity-0 group-hover:opacity-100 transition-opacity">
        {formatTimeShort(message.sentAt)}
      </span>
    </div>
  );
};
const ConversationItem: React.FC<{
  conversation: ConversationSummary;
  active: boolean;
  onSelect: () => void;
  currentUserId?: string;
  participantProfiles: Record<string, ParticipantProfile>;
}> = ({ conversation, active, onSelect, currentUserId, participantProfiles }) => {
  const otherRaw = getOtherParticipant(conversation, currentUserId);
  const profile = otherRaw?.id ? participantProfiles[otherRaw.id] : undefined;
  const other =
    otherRaw && (profile?.displayName || profile?.avatarUrl)
      ? {
          ...otherRaw,
          displayName: otherRaw.displayName || otherRaw.storeName || profile?.displayName,
          avatarUrl: otherRaw.avatarUrl || profile?.avatarUrl,
        }
      : otherRaw;
  const last = conversation.lastMessage;
  const isUnread = (conversation.unreadCount || 0) > 0;
  const otherName = resolveParticipantName(other, other?.id);

  return (
    <div
      onClick={onSelect}
      className={`relative group flex cursor-pointer items-center gap-3 rounded-xl p-3 mx-2 transition-all duration-200 ${
        active 
          ? "bg-emerald-50/80 shadow-sm ring-1 ring-emerald-100/50" 
          : "hover:bg-zinc-50 hover:shadow-sm"
      }`}
    >
      <div className="relative shrink-0">
        <Avatar className="h-12 w-12 border-2 border-white shadow-sm group-hover:border-emerald-100 transition-colors">
          <AvatarImage src={other?.avatarUrl} />
          <AvatarFallback className="bg-linear-to-br from-zinc-100 to-zinc-200 text-zinc-600 font-semibold">
            {otherName.trim().charAt(0).toUpperCase() || <User size={20} />}
          </AvatarFallback>
        </Avatar>
        {/* Mock Online Indicator */}
        <span className="absolute bottom-0.5 right-0.5 h-3 w-3 rounded-full bg-emerald-500 ring-2 ring-white shadow-sm" />
      </div>

      <div className="flex-1 min-w-0">
        <div className="flex justify-between items-center mb-0.5">
           <h4 className={`text-[14px] truncate ${isUnread ? "font-bold text-zinc-900" : "font-semibold text-zinc-700"}`}>
             {otherName}
           </h4>
           {last && (
             <span className={`text-[10px] flex-none ml-2 ${isUnread ? "font-bold text-emerald-600" : "text-zinc-400"}`}>
               {formatTimeShort(last.sentAt)}
             </span>
           )}
        </div>
        
        <div className="flex items-center gap-2">
          <p className={`truncate text-[13px] leading-tight flex-1 ${isUnread ? "font-semibold text-zinc-900" : "text-zinc-500"}`}>
            {last ? (
              <>
                {last.senderId === currentUserId && "You: "}
                {isImageContent(last.content) ? "Photo" : last.content}
              </>
            ) : (
              <span className="italic opacity-80">Start a conversation</span>
            )}
          </p>
          {isUnread && (
            <span className="flex h-5 min-w-5 items-center justify-center rounded-full bg-emerald-500 px-1.5 text-[10px] font-bold text-white shadow-sm">
              {conversation.unreadCount}
            </span>
          )}
        </div>
      </div>
    </div>
  );
};

export const ChatWidget: React.FC<ChatWidgetProps> = ({ fullPage = false, initialTargetUserId }) => {
   
  const { user} = useAuth();
  const isAuthenticated = Boolean(user);
  const router = useRouter();
  const pathname = usePathname();
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
  const [composeTarget, setComposeTarget] = useState(initialTargetUserId ?? "");
  const [isOpen, setIsOpen] = useState(fullPage || Boolean(initialTargetUserId)); // isOpen can be true if fullPage or initialTargetUserId is present

  const [draft, setDraft] = useState(""); // Moved draft declaration here
  const [uploadingImage, setUploadingImage] = useState(false);
  const [participantProfiles, setParticipantProfiles] = useState<Record<string, ParticipantProfile>>({});

  const messagesEndRef = useRef<HTMLDivElement | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const userClearedSelection = useRef(false);

  // --- Derived State ---

  const activeConversation = useMemo(
    () => conversations.find((c) => c.id === activeConversationId),
    [activeConversationId, conversations],
  );

  const otherParticipant = useMemo(() => {
    const raw = getOtherParticipant(activeConversation, user?.id);
    if (!raw) return null;
    const profile = raw.id ? participantProfiles[raw.id] : undefined;
    if (!profile) return raw;
    return {
      ...raw,
      displayName: raw.displayName || raw.storeName || profile.displayName,
      avatarUrl: raw.avatarUrl || profile.avatarUrl,
    };
  }, [activeConversation, participantProfiles, user?.id]);

  const derivedConversationKey = useMemo(
    () => activeConversationId ?? (composeTarget ? `temp:${composeTarget}` : null),
    [activeConversationId, composeTarget],
  );

  const messages = useMemo(
    () => getMessagesForConversation(derivedConversationKey),
    [derivedConversationKey, getMessagesForConversation],
  );

  const filteredConversations = useMemo(
    () =>
      conversations.filter((conversation) => {
        const otherRaw = getOtherParticipant(conversation, user?.id);
        const profile = otherRaw?.id ? participantProfiles[otherRaw.id] : undefined;
        const other = otherRaw
          ? {
              ...otherRaw,
              displayName: otherRaw.displayName || otherRaw.storeName || profile?.displayName,
              avatarUrl: otherRaw.avatarUrl || profile?.avatarUrl,
            }
          : otherRaw;
        if (!filter.trim()) return true;
        return (
          other?.displayName?.toLowerCase().includes(filter.toLowerCase()) ||
          other?.storeName?.toLowerCase().includes(filter.toLowerCase()) ||
          profile?.displayName?.toLowerCase().includes(filter.toLowerCase()) ||
          other?.id?.toLowerCase().includes(filter.toLowerCase())
        );
      }),
    [conversations, filter, participantProfiles, user?.id],
  );

  const unreadTotal = useMemo(
    () => conversations.reduce((sum, conv) => sum + (conv.unreadCount ?? 0), 0),
    [conversations],
  );

  useEffect(() => {
    if (!isAuthenticated) return;
    if (activeConversationId) {
      userClearedSelection.current = false;
      return;
    }
    if (userClearedSelection.current) return;
    if (activeConversationId) return;
    if (conversations.length > 0 && !composeTarget) {

      setActiveConversationId(conversations[0].id);
    } else if (composeTarget) {

      setActiveConversationId(`temp:${composeTarget}`);
    }
  }, [activeConversationId, composeTarget, conversations, isAuthenticated, setActiveConversationId]);

  useEffect(() => {
    if (initialTargetUserId) {
      setComposeTarget(initialTargetUserId);
      if (!activeConversationId) {

        setActiveConversationId(`temp:${initialTargetUserId}`);
      }

      setIsOpen(true);
      userClearedSelection.current = false;
    }
  }, [activeConversationId, initialTargetUserId, setActiveConversationId]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages.length, activeConversationId]);

  useEffect(() => {
    const missingIds = new Set<string>();
    conversations.forEach((conv) =>
      conv.participants.forEach((p) => {
        if (!p?.id) return;
        const hasName = p.storeName || p.displayName || participantProfiles[p.id]?.displayName;
        if (!hasName && !participantProfiles[p.id]) {
          missingIds.add(p.id);
        }
      }),
    );
    if (missingIds.size === 0) return;
    let cancelled = false;
    const load = async () => {
      const results = await Promise.all(
        Array.from(missingIds).map(async (id) => {
          try {
            const profile = await userApi.getById(id);
            return { id, displayName: profile.displayName, avatarUrl: profile.avatarUrl };
          } catch {
            return null;
          }
        }),
      );
      if (cancelled) return;
      setParticipantProfiles((prev) => {
        const next = { ...prev };
        results.forEach((res) => {
          if (res) next[res.id] = { displayName: res.displayName, avatarUrl: res.avatarUrl };
        });
        return next;
      });
    };
    void load();
    return () => {
      cancelled = true;
    };
  }, [conversations, participantProfiles]);

  const resolveReceiverId = () => otherParticipant?.id || composeTarget;

  const sendContent = async (content: string) => {
    const trimmed = content.trim();
    if (!trimmed) return;

    if (connectionStatus !== "connected") {
      addToast("Connecting...", "info");
    }

    const receiverId = resolveReceiverId();
    if (!receiverId) {
      addToast("Please choose a recipient before sending", "error");
      return;
    }

    try {
      await sendMessage({
        conversationId: activeConversation?.id,
        receiverId,
        content: trimmed,
      });
      setDraft("");
      inputRef.current?.focus();
    } catch (_error: unknown) { // eslint-disable-line @typescript-eslint/no-unused-vars
      addToast("Failed to send", "error");
    }
  };

  const handleSend = async () => sendContent(draft);

  const handleSelectImage = () => fileInputRef.current?.click();

  const handleImageChosen = async (file?: File) => {
    if (!file) return;
    setUploadingImage(true);
    try {
      const imageUrl = await uploadToBucket(config.supabaseChatBucket, file);
      await sendContent(imageUrl);
    } catch (error) {
      const message = error instanceof Error ? error.message : "Upload image failed";
      addToast(message, "error");
    } finally {
      setUploadingImage(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  };

  const loginHref = `/login?next=${encodeURIComponent(pathname)}`;

  // --- Views ---

  const renderConversationList = () => (
    <div className="flex h-full flex-col bg-white">
      {/* Header */}
      <div className="flex items-center justify-between px-4 py-4 border-b border-zinc-100/50 bg-white/80 backdrop-blur-md sticky top-0 z-10">
        <h1 className="text-xl font-bold text-zinc-800 tracking-tight">Chats</h1>
        <div className="flex gap-1">
           <Button variant="ghost" size="icon" className="h-8 w-8 rounded-full text-zinc-500 hover:bg-zinc-100">
              <MoreHorizontal size={20} />
           </Button>
           {!fullPage && (
               <Button 
                variant="ghost" 
                size="icon" 
                className="h-8 w-8 rounded-full text-zinc-400 hover:bg-red-50 hover:text-red-500 transition-colors"
                onClick={() => setIsOpen(false)}
               >
                  <Minimize2 size={18} />
               </Button>
           )}
        </div>
      </div>

      {/* Search */}
      <div className="px-3 py-2">
        <div className="relative group">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400 group-focus-within:text-emerald-500 transition-colors" size={16} />
          <Input
            className="h-10 pl-10 rounded-full bg-zinc-100 border-transparent focus:border-emerald-500/20 focus:bg-white focus:ring-4 focus:ring-emerald-500/10 transition-all text-[14px]"
            placeholder="Search Messenger"
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
          />
        </div>
      </div>

      {/* List */}
          <div className="flex-1 overflow-y-auto pt-2 space-y-1 custom-scrollbar scroll-smooth">
        {loadingConversations && conversations.length === 0 ? (
           <div className="flex justify-center py-8"><Loader2 className="animate-spin text-emerald-500" /></div>
        ) : filteredConversations.length === 0 ? (
           <div className="flex flex-col items-center justify-center h-40 text-center px-6">
              <div className="bg-zinc-50 p-3 rounded-full mb-3">
                 <MessageCircle className="text-zinc-300" size={24}/>
              </div>
              <p className="text-zinc-500 text-sm font-medium">No messages yet</p>
              <p className="text-zinc-400 text-xs mt-1">Start chatting with sellers!</p>
           </div>
        ) : (
            filteredConversations.map(conv => (
                <ConversationItem 
                    key={conv.id}
                    conversation={conv}
                    active={activeConversationId === conv.id}
                    onSelect={() => {
                        setComposeTarget("");
                        userClearedSelection.current = false;
                        setActiveConversationId(conv.id);
                    }}
                    currentUserId={user?.id}
                    participantProfiles={participantProfiles}
                />
            ))
        )}
      </div>

      {/* New Chat Input */}
      <div className="p-3 border-t border-zinc-100 bg-zinc-50/50">
         <div className="flex items-center gap-2 bg-white p-1.5 rounded-lg border border-zinc-200 shadow-sm">
             <span className="text-[10px] font-bold text-zinc-400 uppercase tracking-wider px-2">TO:</span>
             <input 
                className="flex-1 text-sm bg-transparent outline-none placeholder:text-zinc-300 text-zinc-700 min-w-0"
                placeholder="User ID..."
                value={composeTarget}
                onChange={(e) => {
                    setComposeTarget(e.target.value);
                    if (e.target.value) {
                        userClearedSelection.current = false;
                        setActiveConversationId(`temp:${e.target.value}`);
                    }
                }}
             />
         </div>
      </div>
    </div>
  );

  const renderChatArea = () => {
     if (!activeConversationId && !composeTarget) {
         return (
             <div className="flex h-full flex-col items-center justify-center bg-zinc-50/50 text-center p-6">
                 <div className="h-24 w-24 bg-white rounded-full shadow-sm flex items-center justify-center mb-6 ring-1 ring-zinc-100">
                     <MessageCircle size={40} className="text-emerald-500" />
                 </div>
                 <h3 className="text-xl font-bold text-zinc-900">Welcome to Chat</h3>
                 <p className="text-zinc-500 text-sm mt-2 max-w-xs leading-relaxed">
                    Select a conversation from the left to start messaging securely with sellers.
                 </p>
             </div>
         );
     }

      const displayName = resolveParticipantName(
        otherParticipant,
        otherParticipant?.id || composeTarget || "Shop",
      );
     const isSeller = otherParticipant?.role === "SELLER";

     return (
         <div className="flex h-full flex-col bg-[#f8f9fa] relative">
             {/* Chat Header */}
             <div className="flex items-center justify-between px-4 py-3 bg-white/90 backdrop-blur-md border-b border-zinc-200/60 shadow-sm z-20 sticky top-0">
                 <div className="flex items-center gap-3">
                     {!fullPage && (
                          <Button 
                             variant="ghost" 
                             size="icon" 
                             className="h-9 w-9 text-emerald-600 hover:bg-emerald-50 rounded-full -ml-2"
                             onClick={() => {
                                userClearedSelection.current = true;
                                setActiveConversationId(null);
                                setComposeTarget("");
                             }}
                          >
                              <ArrowLeft size={20} />
                         </Button>
                     )}
                     
                      <div className="relative">
                        <Avatar className="h-10 w-10 border border-zinc-100 shadow-sm">
                            <AvatarImage src={otherParticipant?.avatarUrl} />
                            <AvatarFallback className="bg-linear-to-br from-emerald-100 to-emerald-200 text-emerald-700 font-bold">
                                {displayName.trim().charAt(0)}
                            </AvatarFallback>
                        </Avatar>
                        {connectionStatus === "connected" && (
                            <span className="absolute bottom-0 right-0 h-3 w-3 bg-emerald-500 border-2 border-white rounded-full shadow-sm"></span>
                        )}
                     </div>
                     
                     <div className="flex flex-col">
                         <span className="text-[15px] font-bold text-zinc-900 leading-none flex items-center gap-1.5">
                            {displayName}
                            {isSeller && <span className="text-[9px] bg-orange-50 text-orange-600 px-1.5 py-0.5 rounded-md font-extrabold tracking-wide border border-orange-100">SELLER</span>}
                         </span>
                         <span className="text-[11px] font-medium text-zinc-400 mt-0.5">
                             {connectionStatus === "connected" ? "Active now" : "Offline"}
                         </span>
                     </div>
                 </div>

                 <div className="flex items-center gap-1 text-emerald-600">
                     <Button variant="ghost" size="icon" className="h-9 w-9 rounded-full hover:bg-emerald-50 text-emerald-600"><Phone size={18} /></Button>
                     <Button variant="ghost" size="icon" className="h-9 w-9 rounded-full hover:bg-emerald-50 text-emerald-600"><Video size={20} /></Button>
                     {!fullPage && (
                         <Button 
                            variant="ghost" 
                            size="icon" 
                            className="h-9 w-9 rounded-full hover:bg-red-50 text-zinc-400 hover:text-red-500 transition-colors"
                            onClick={() => setIsOpen(false)}
                         >
                            <X size={20} />
                         </Button>
                     )}
                 </div>
             </div>

             {/* Messages Area */}
             <div className="flex-1 overflow-y-auto p-4 custom-scrollbar bg-slate-50">
                 {loadingMessages && messages.length === 0 ? (
                     <div className="flex h-full items-center justify-center"><Loader2 className="animate-spin text-emerald-500 h-8 w-8" /></div>
                 ) : (
                     <>
                        {/* Initial User Info Header */}
                        <div className="flex flex-col items-center py-8 gap-3 opacity-80 mb-6">
                              <Avatar className="h-20 w-20 ring-4 ring-white shadow-md">
                                <AvatarImage src={otherParticipant?.avatarUrl} />
                                <AvatarFallback className="text-2xl bg-zinc-100 text-zinc-400">
                                  {displayName.trim().charAt(0)}
                                </AvatarFallback>
                              </Avatar>
                             <div className="text-center">
                                 <h3 className="text-lg font-bold text-zinc-900">{displayName}</h3>
                                 <p className="text-xs text-zinc-500 font-medium">EcomX Marketplace &bull; Connected</p>
                             </div>
                        </div>

                        {messages.map((msg, idx) => {
                            const isMine = msg.senderId === user?.id;
                            const prevMsg = messages[idx - 1];
                            const nextMsg = messages[idx + 1];
                            
                            // Check for new group based on sender and time (still useful for bubble rounding)
                            const isFirstInGroup = !prevMsg || prevMsg.senderId !== msg.senderId || (new Date(msg.sentAt).getTime() - new Date(prevMsg.sentAt).getTime() > (5 * 60 * 1000));
                            const isLastInGroup = !nextMsg || nextMsg.senderId !== msg.senderId || (new Date(nextMsg.sentAt).getTime() - new Date(msg.sentAt).getTime() > (5 * 60 * 1000));

                            return (
                                <MessageBubble
                                    key={msg.id}
                                    message={msg}
                                    isMine={isMine}
                                    isFirstInGroup={isFirstInGroup}
                                    isLastInGroup={isLastInGroup}
                                    showAvatar={!isMine} // Always show avatar for incoming messages
                                    senderAvatar={otherParticipant?.avatarUrl}
                                    senderName={displayName}
                                />
                            );
                        })}
                        <div ref={messagesEndRef} className="h-2" />
                     </>
                 )}
             </div>

             {/* Input Area */}
             <div className="p-3 bg-white border-t border-zinc-100">
                 <div className="flex items-end gap-2 max-w-full">
                      <div className="flex gap-1 pb-2 text-emerald-600 shrink-0">
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-9 w-9 rounded-full hover:bg-emerald-50 transition-colors"
                            onClick={handleSelectImage}
                            disabled={uploadingImage}
                          >
                            {uploadingImage ? <Loader2 className="h-4 w-4 animate-spin" /> : <ImageIcon size={20} />}
                          </Button>
                          <Button variant="ghost" size="icon" className="h-9 w-9 rounded-full hover:bg-emerald-50 transition-colors"><Smile size={20} /></Button>
                      </div>
                     
                      <div className="flex-1 relative min-w-0">
                          <Input
                              ref={inputRef}
                              className="w-full rounded-3xl bg-zinc-100 border-transparent py-2.5 px-4 focus:bg-white focus:border-emerald-200 focus:ring-4 focus:ring-emerald-500/10 transition-all shadow-sm min-h-[42px] max-h-[120px] text-[15px] resize-none overflow-hidden"
                              placeholder="Type a message..."
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
                      </div>

                      <Button 
                         size="icon" 
                         variant="ghost"
                         onClick={handleSend}
                         disabled={!draft.trim() && !uploadingImage}
                         className={`h-10 w-10 rounded-full mb-0.5 shrink-0 transition-all duration-300 ${
                             draft.trim() 
                                 ? "bg-emerald-600 text-white hover:bg-emerald-700 shadow-md hover:shadow-lg transform hover:-translate-y-0.5" 
                                 : "bg-transparent text-zinc-300 hover:bg-zinc-50"
                        }`}
                      >
                          <Send size={18} fill={draft.trim() ? "currentColor" : "none"} className={draft.trim() ? "ml-0.5" : ""} />
                      </Button>
                  </div>
                  <input
                    type="file"
                    accept="image/*"
                    ref={fileInputRef}
                    className="hidden"
                    onChange={(e) => handleImageChosen(e.target.files?.[0])}
                  />
              </div>
          </div>
     );
  };


  // --- Main Render ---

  if (fullPage) {
      return (
        <div className="container mx-auto max-w-7xl h-[calc(100vh-80px)] p-4 md:p-6">
             <div className="flex h-full overflow-hidden rounded-2xl border border-zinc-200/80 shadow-2xl bg-white ring-1 ring-black/5">
                 <div className="w-[380px] border-r border-zinc-100 flex flex-col bg-white">
                     {renderConversationList()}
                 </div>
                 <div className="flex-1 flex flex-col min-w-0">
                     {renderChatArea()}
                 </div>
             </div>
        </div>
      );
  }

  // Floating Widget Mode
  return (
    <>
        {/* Toggle Button */}
        {!isOpen && (
            <div className="fixed bottom-6 right-6 z-50 flex flex-col items-end gap-3 group">
                 {unreadTotal > 0 && (
                    <div className="bg-white px-4 py-2 rounded-xl shadow-lg border border-zinc-100 mb-2 animate-in slide-in-from-bottom-2 fade-in duration-300 origin-bottom-right">
                        <p className="text-sm font-semibold text-zinc-800">You have <span className="text-emerald-600">{unreadTotal}</span> new messages</p>
                    </div>
                )}
                <Button
                    onClick={() => setIsOpen(true)}
                    className="h-16 w-16 rounded-full shadow-2xl bg-linear-to-br from-emerald-500 to-emerald-700 hover:scale-110 transition-all duration-300 relative"
                >
                    <MessageSquareMore size={30} className="text-white" />
                    {unreadTotal > 0 && (
                        <span className="absolute 0 top-0 right-0 flex h-6 w-6 items-center justify-center rounded-full bg-red-500 ring-4 ring-white text-[11px] font-bold text-white shadow-sm">
                            {unreadTotal}
                        </span>
                    )}
                </Button>
            </div>
        )}

        {/* Widget Window */}
        <div 
            className={`
                fixed bottom-0 right-0 z-50 flex flex-col bg-white shadow-[0_0_40px_-10px_rgba(0,0,0,0.15)] rounded-t-2xl overflow-hidden border border-zinc-200/80
                transition-all duration-400 cubic-bezier(0.16, 1, 0.3, 1) origin-bottom-right
                ${isOpen ? "opacity-100 translate-y-0 scale-100" : "opacity-0 translate-y-12 scale-95 pointer-events-none"}
            `}
            style={{
                width: "420px",
                height: "640px",
                maxHeight: "calc(100vh - 40px)",
                bottom: 0,
                right: 24, // 1.5rem
                borderBottomLeftRadius: 0,
                borderBottomRightRadius: 0
            }}
        >
             {!isAuthenticated ? (
                 <div className="flex flex-col h-full bg-linear-to-b from-emerald-50 to-white">
                     <div className="flex justify-between items-center p-4">
                         <span className="font-bold text-emerald-800 tracking-tight text-lg">EcomX Support</span>
                         <Button variant="ghost" size="sm" onClick={() => setIsOpen(false)} className="hover:bg-black/5 rounded-full h-8 w-8 p-0"><X size={20}/></Button>
                     </div>
                     <div className="flex-1 flex flex-col items-center justify-center p-8 text-center gap-6">
                         <div className="h-20 w-20 bg-white rounded-2xl shadow-lg flex items-center justify-center rotate-3 ring-1 ring-emerald-100">
                             <MessageCircle size={40} className="text-emerald-500 fill-emerald-50" />
                         </div>
                         <div className="space-y-2">
                            <h3 className="font-bold text-xl text-zinc-900">Hello there! 👋</h3>
                            <p className="text-sm text-zinc-500 leading-relaxed max-w-60 mx-auto">
                                Sign in to start chatting with sellers, track orders, and get support.
                            </p>
                         </div>
                         <Button className="w-full max-w-[200px] bg-emerald-600 hover:bg-emerald-700 shadow-lg shadow-emerald-200" onClick={() => router.push(loginHref)}>
                             Log In Now
                         </Button>
                     </div>
                 </div>
             ) : (
                activeConversationId || composeTarget ? (
                    renderChatArea()
                ) : (
                    renderConversationList()
                )
             )}
        </div>
    </>
  );
};



