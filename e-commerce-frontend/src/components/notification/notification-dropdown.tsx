'use client';

import React, { useState, useEffect } from 'react';
import { Bell, Check, X, Package, CreditCard, Truck } from 'lucide-react';
import { useAuth } from '@/src/store/auth-store';
import { notificationApi } from '@/src/api/notificationApi';
import { useNotificationSocket, Notification } from '@/src/hooks/use-notification-socket';
import { formatDistanceToNow } from 'date-fns';
import { vi } from 'date-fns/locale';

export function NotificationDropdown() {
    const { user } = useAuth();
    const [isOpen, setIsOpen] = useState(false);
    const [notifications, setNotifications] = useState<Notification[]>([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [loading, setLoading] = useState(false);

    // Load initial notifications
    useEffect(() => {
        if (user && isOpen) {
            loadNotifications();
        }
    }, [user, isOpen]);

    // Subscribe to real-time notifications
    useNotificationSocket({
        enabled: !!user,
        onNotification: (notification) => {
            setNotifications((prev) => [notification, ...prev]);
            if (!notification.read) {
                setUnreadCount((prev) => prev + 1);
            }

            // Show browser notification if permission granted
            if ('Notification' in window && Notification.permission === 'granted') {
                new Notification(notification.title, {
                    body: notification.message,
                    icon: '/logo.png', // Add your logo
                });
            }
        },
    });

    const loadNotifications = async () => {
        if (!user) return;
        setLoading(true);
        try {
            const [notifs, count] = await Promise.all([
                notificationApi.list(user.id, 20),
                notificationApi.unreadCount(user.id),
            ]);
            setNotifications(notifs);
            setUnreadCount(count);
        } catch (error) {
            console.error('Failed to load notifications:', error);
        } finally {
            setLoading(false);
        }
    };

    const markAsRead = async (id: string) => {
        if (!user) return;
        try {
            await notificationApi.markRead(id, user.id);
            setNotifications((prev) =>
                prev.map((n) => (n.id === id ? { ...n, read: true } : n))
            );
            setUnreadCount((prev) => Math.max(0, prev - 1));
        } catch (error) {
            console.error('Failed to mark notification as read:', error);
        }
    };

    const getIcon = (type: string) => {
        switch (type) {
            case 'ORDER_CREATED':
            case 'ORDER_UPDATED':
                return <Package size={18} className="text-blue-600" />;
            case 'PAYMENT_SUCCESS':
            case 'PAYMENT_FAILED':
                return <CreditCard size={18} className="text-green-600" />;
            case 'SHIPPING_UPDATE':
                return <Truck size={18} className="text-purple-600" />;
            default:
                return <Bell size={18} className="text-zinc-600" />;
        }
    };

    // Request notification permission on mount
    useEffect(() => {
        if ('Notification' in window && Notification.permission === 'default') {
            Notification.requestPermission();
        }
    }, []);

    if (!user) return null;

    return (
        <div className="relative">
            {/* Bell Icon Button */}
            <button
                onClick={() => setIsOpen(!isOpen)}
                className="relative rounded-lg p-2 text-zinc-700 transition-colors hover:bg-zinc-100 hover:text-emerald-600"
            >
                <Bell size={22} />
                {unreadCount > 0 && (
                    <span className="absolute right-0 top-0 flex h-5 w-5 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white">
                        {unreadCount > 9 ? '9+' : unreadCount}
                    </span>
                )}
            </button>

            {/* Dropdown */}
            {isOpen && (
                <>
                    {/* Overlay for mobile */}
                    <div
                        className="fixed inset-0 z-40 md:hidden"
                        onClick={() => setIsOpen(false)}
                    />

                    {/* Dropdown Panel */}
                    <div className="absolute right-0 top-full z-50 mt-2 w-96 max-w-[calc(100vw-2rem)] rounded-xl border border-zinc-200 bg-white shadow-2xl">
                        {/* Header */}
                        <div className="flex items-center justify-between border-b border-zinc-200 p-4">
                            <h3 className="font-semibold text-zinc-900">Thông báo</h3>
                            <button
                                onClick={() => setIsOpen(false)}
                                className="rounded-lg p-1.5 text-zinc-500 transition-colors hover:bg-zinc-100 hover:text-zinc-900"
                            >
                                <X size={18} />
                            </button>
                        </div>

                        {/* Notifications List */}
                        <div className="max-h-[400px] overflow-y-auto">
                            {loading ? (
                                <div className="flex items-center justify-center p-8 text-sm text-zinc-500">
                                    Đang tải...
                                </div>
                            ) : notifications.length === 0 ? (
                                <div className="flex flex-col items-center justify-center p-8 text-center">
                                    <Bell size={32} className="mb-2 text-zinc-300" />
                                    <p className="text-sm text-zinc-500">Không có thông báo</p>
                                </div>
                            ) : (
                                <div className="divide-y divide-zinc-100">
                                    {notifications.map((notif) => (
                                        <div
                                            key={notif.id}
                                            className={`group relative px-4 py-3 transition-colors hover:bg-zinc-50 ${!notif.read ? 'bg-emerald-50/30' : ''
                                                }`}
                                        >
                                            <div className="flex gap-3">
                                                {/* Icon */}
                                                <div className="mt-0.5 flex-shrink-0">
                                                    {getIcon(notif.type)}
                                                </div>

                                                {/* Content */}
                                                <div className="flex-1 min-w-0">
                                                    <p className="text-sm font-semibold text-zinc-900">
                                                        {notif.title}
                                                    </p>
                                                    <p className="mt-0.5 text-xs text-zinc-600">
                                                        {notif.message}
                                                    </p>
                                                    <p className="mt-1 text-xs text-zinc-400">
                                                        {formatDistanceToNow(new Date(notif.createdAt), {
                                                            addSuffix: true,
                                                            locale: vi,
                                                        })}
                                                    </p>
                                                </div>

                                                {/* Mark as read button */}
                                                {!notif.read && (
                                                    <button
                                                        onClick={() => markAsRead(notif.id)}
                                                        className="flex-shrink-0 rounded-lg p-1.5 text-emerald-600 opacity-0 transition-opacity hover:bg-emerald-100 group-hover:opacity-100"
                                                        title="Đánh dấu đã đọc"
                                                    >
                                                        <Check size={16} />
                                                    </button>
                                                )}
                                            </div>

                                            {/* Unread indicator */}
                                            {!notif.read && (
                                                <div className="absolute left-1.5 top-1/2 h-2 w-2 -translate-y-1/2 rounded-full bg-emerald-500" />
                                            )}
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>

                        {/* Footer */}
                        {notifications.length > 0 && (
                            <div className="border-t border-zinc-200 p-3 text-center">
                                <button className="text-sm font-medium text-emerald-600 hover:underline">
                                    Xem tất cả
                                </button>
                            </div>
                        )}
                    </div>
                </>
            )}
        </div>
    );
}
