'use client';

import React, { useEffect } from "react";
import { useNotifications } from "@/src/store/notification-store";
import { useAuth } from "@/src/store/auth-store";
import { Button } from "@/src/components/ui/button";
import { Skeleton } from "@/src/components/ui/skeleton";
import { useTranslation } from "@/src/providers/language-provider";

export default function NotificationsPage() {
  const { user } = useAuth();
  const { notifications, loading, refresh, markRead } = useNotifications();
  const { t } = useTranslation();

  useEffect(() => {
    void refresh();
  }, [refresh]);

  if (!user) {
    return <div className="container mx-auto max-w-3xl py-10 text-center text-zinc-600">{t.notifications.login_required}</div>;
  }

  return (
    <div className="container mx-auto max-w-3xl py-8 px-4 space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-zinc-900">{t.notifications.title}</h1>
        <Button variant="outline" size="sm" onClick={() => void refresh()}>{t.notifications.refresh}</Button>
      </div>

      <div className="bg-white border border-zinc-200 rounded-xl shadow-sm divide-y divide-zinc-100">
        {loading ? (
          <div className="p-4 space-y-3">
            {[...Array(4)].map((_, idx) => (
              <div key={idx} className="flex gap-3">
                <Skeleton className="h-10 w-10 rounded-full" />
                <div className="flex-1 space-y-2">
                  <Skeleton className="h-4 w-40" />
                  <Skeleton className="h-3 w-56" />
                </div>
              </div>
            ))}
          </div>
        ) : notifications.length === 0 ? (
          <div className="p-6 text-center text-sm text-zinc-500">{t.notifications.empty}</div>
        ) : (
          notifications.map((n) => (
            <div key={n.id} className={`flex items-start gap-3 px-4 py-3 ${n.status === "UNREAD" ? "bg-emerald-50/40" : ""}`}>
              <div className="mt-1 h-2 w-2 rounded-full" style={{ backgroundColor: n.status === "UNREAD" ? "#059669" : "#e5e7eb" }} />
              <div className="flex-1">
                <div className="font-semibold text-sm text-zinc-800">{n.title}</div>
                <div className="text-xs text-zinc-500">{n.body}</div>
              </div>
              {n.status === "UNREAD" && (
                <Button size="sm" variant="ghost" className="text-xs text-emerald-600" onClick={() => void markRead(n.id)}>
                  {t.notifications.mark_read}
                </Button>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}
