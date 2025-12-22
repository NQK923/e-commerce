'use client';

import React, { useEffect, useState } from "react";
import { Bell, CheckCheck } from "lucide-react";
import Link from "next/link";
import { useNotifications } from "@/src/store/notification-store";
import { useAuth } from "@/src/store/auth-store";
import { Button } from "../ui/button";
import { Skeleton } from "../ui/skeleton";
import { useTranslation } from "@/src/providers/language-provider";

export const NotificationBell: React.FC = () => {
  const { user } = useAuth();
  const { notifications, unreadCount, loading, markRead, refresh } = useNotifications();
  const [open, setOpen] = useState(false);
  const { t } = useTranslation();

  useEffect(() => {
    if (open) void refresh();
  }, [open, refresh]);

  if (!user) return null;

  return (
    <div className="relative">
      <button
        className="relative p-2 text-zinc-600 hover:text-emerald-600 transition-colors"
        onClick={() => setOpen((prev) => !prev)}
        aria-label={t.notifications.title}
      >
        <Bell size={20} />
        {unreadCount > 0 && (
          <span className="absolute -right-1 -top-1 flex h-4 w-4 items-center justify-center rounded-full bg-emerald-600 text-[9px] font-bold text-white ring-2 ring-white shadow-sm">
            {unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 mt-3 w-80 rounded-xl border border-zinc-200 bg-white shadow-xl overflow-hidden z-50">
          <div className="flex items-center justify-between px-4 py-3 border-b border-zinc-100">
            <div className="font-semibold text-zinc-800 text-sm">{t.notifications.title}</div>
            <Button variant="ghost" size="sm" onClick={() => void refresh()} className="text-xs text-emerald-600">
              {t.notifications.refresh}
            </Button>
          </div>
          <div className="max-h-96 overflow-y-auto">
            {loading ? (
              <div className="p-4 space-y-3">
                {[...Array(3)].map((_, idx) => (
                  <div key={idx} className="flex gap-3">
                    <Skeleton className="h-10 w-10 rounded-full" />
                    <div className="flex-1 space-y-2">
                      <Skeleton className="h-3 w-40" />
                      <Skeleton className="h-3 w-24" />
                    </div>
                  </div>
                ))}
              </div>
            ) : notifications.length === 0 ? (
              <div className="p-4 text-sm text-zinc-500 text-center">{t.notifications.empty}</div>
            ) : (
              notifications.map((n) => (
                <div
                  key={n.id}
                  className={`flex gap-3 px-4 py-3 hover:bg-zinc-50 ${n.status === "UNREAD" ? "bg-emerald-50/40" : ""}`}
                >
                  <div className="mt-1">
                    {n.status === "UNREAD" ? (
                      <span className="inline-flex h-2 w-2 rounded-full bg-emerald-600" />
                    ) : (
                      <CheckCheck size={14} className="text-zinc-300" />
                    )}
                  </div>
                  <div className="flex-1">
                    <div className="text-sm font-semibold text-zinc-800">{n.title}</div>
                    <div className="text-xs text-zinc-500">{n.body}</div>
                  </div>
                  {n.status === "UNREAD" && (
                    <Button variant="ghost" size="sm" className="text-xs text-emerald-600" onClick={() => void markRead(n.id)}>
                      {t.notifications.mark_read}
                    </Button>
                  )}
                </div>
              ))
            )}
          </div>
          <div className="border-t border-zinc-100 px-4 py-2 text-right text-xs">
            <Link href="/notifications" className="text-emerald-600 hover:text-emerald-700">{t.notifications.view_all}</Link>
          </div>
        </div>
      )}
    </div>
  );
};
