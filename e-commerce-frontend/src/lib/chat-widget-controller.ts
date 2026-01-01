'use client';

type ChatOpenDetail = {
  userId?: string;
  conversationId?: string;
};

const OPEN_EVENT = "chat-widget:open";
const CLOSE_EVENT = "chat-widget:close";

const dispatch = (eventName: string, detail?: ChatOpenDetail) => {
  if (typeof window === "undefined") return;
  window.dispatchEvent(new CustomEvent<ChatOpenDetail>(eventName, { detail }));
};

export const openChatWidget = (options?: ChatOpenDetail) => dispatch(OPEN_EVENT, options);
export const closeChatWidget = () => dispatch(CLOSE_EVENT);
