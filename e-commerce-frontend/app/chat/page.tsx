'use client';

import React from "react";
import { useSearchParams } from "next/navigation";
import { ChatWidget } from "@/src/components/chat/chat-widget";

export default function ChatPage() {
  const searchParams = useSearchParams();
  const initialTargetUserId = searchParams.get("userId");

  return <ChatWidget fullPage initialTargetUserId={initialTargetUserId} />;
}
