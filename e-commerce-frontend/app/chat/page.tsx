'use client';

import { useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { openChatWidget } from "@/src/lib/chat-widget-controller";

export default function ChatPage() {
  const searchParams = useSearchParams();
  const router = useRouter();

  useEffect(() => {
    const userId = searchParams.get("userId") ?? undefined;
    openChatWidget({ userId });
    if (window.history.length > 1) {
      router.back();
    } else {
      router.replace("/");
    }
  }, [router, searchParams]);

  return null;
}
