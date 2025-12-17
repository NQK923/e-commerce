'use client';

import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { notificationApi } from "../api/notificationApi";
import { Notification } from "../types/notification";
import { useAuth } from "./auth-store";

type NotificationContextValue = {
  notifications: Notification[];
  unreadCount: number;
  loading: boolean;
  refresh: () => Promise<void>;
  markRead: (id: string) => Promise<void>;
};

const NotificationContext = createContext<NotificationContextValue | undefined>(undefined);

export const NotificationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    if (!user?.id) return;
    setLoading(true);
    try {
      const [items, unread] = await Promise.all([
        notificationApi.list(user.id, 20),
        notificationApi.unreadCount(user.id),
      ]);
      setNotifications(items);
      setUnreadCount(unread ?? 0);
    } catch {
      // ignore failures for now
    } finally {
      setLoading(false);
    }
  }, [user?.id]);

  useEffect(() => {
    void load();
  }, [load]);

  const markRead = useCallback(async (id: string) => {
    if (!user?.id) return;
    try {
      await notificationApi.markRead(id, user.id);
      setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, status: "READ", readAt: new Date().toISOString() } : n)));
      setUnreadCount((prev) => (prev > 0 ? prev - 1 : 0));
    } catch {
      // ignore failures
    }
  }, [user?.id]);

  const value = useMemo(() => ({
    notifications,
    unreadCount,
    loading,
    refresh: load,
    markRead,
  }), [notifications, unreadCount, loading, load, markRead]);

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotifications = () => {
  const ctx = useContext(NotificationContext);
  if (!ctx) throw new Error("useNotifications must be used within NotificationProvider");
  return ctx;
};
