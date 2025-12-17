import { apiRequest } from "../lib/api-client";
import { Notification } from "../types/notification";

export const notificationApi = {
  list: (userId: string, limit = 20) =>
    apiRequest<Notification[]>(`/api/notifications?userId=${encodeURIComponent(userId)}&limit=${limit}`),
  unreadCount: (userId: string) =>
    apiRequest<number>(`/api/notifications/unread-count?userId=${encodeURIComponent(userId)}`),
  markRead: (id: string, userId: string) =>
    apiRequest<void>(`/api/notifications/${id}/read?userId=${encodeURIComponent(userId)}`, { method: "POST" }),
};
