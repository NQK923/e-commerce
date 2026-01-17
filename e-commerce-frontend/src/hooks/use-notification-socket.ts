'use client';

import { useEffect, useRef, useCallback } from 'react';
import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuth } from '@/src/store/auth-store';

export interface Notification {
    id: string;
    userId: string;
    type: string;
    title: string;
    message: string;
    read: boolean;
    createdAt: string;
    metadata?: Record<string, any>;
}

interface UseNotificationSocketOptions {
    onNotification?: (notification: Notification) => void;
    enabled?: boolean;
}

/**
 * Hook to subscribe to real-time notifications via WebSocket
 * Reuses existing STOMP configuration from chat
 */
export function useNotificationSocket({
    onNotification,
    enabled = true
}: UseNotificationSocketOptions = {}) {
    const { user, token } = useAuth();
    const clientRef = useRef<Client | null>(null);
    const subscriptionRef = useRef<StompSubscription | null>(null);

    const connect = useCallback(() => {
        if (!user || !token || !enabled) return;

        // Create STOMP client over SockJS
        const client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            connectHeaders: {
                Authorization: `Bearer ${token}`,
            },
            debug: (str) => {
                if (process.env.NODE_ENV === 'development') {
                    console.log('[Notification WebSocket]', str);
                }
            },
            onConnect: () => {
                console.log('[Notification] Connected to WebSocket');

                // Subscribe to user's notification queue
                const subscription = client.subscribe(
                    '/user/queue/notifications',
                    (message) => {
                        try {
                            const notification: Notification = JSON.parse(message.body);
                            console.log('[Notification] Received:', notification);
                            onNotification?.(notification);
                        } catch (error) {
                            console.error('[Notification] Failed to parse notification:', error);
                        }
                    }
                );

                subscriptionRef.current = subscription;
            },
            onDisconnect: () => {
                console.log('[Notification] Disconnected from WebSocket');
            },
            onStompError: (frame) => {
                console.error('[Notification] STOMP error:', frame);
            },
        });

        client.activate();
        clientRef.current = client;
    }, [user, token, enabled, onNotification]);

    const disconnect = useCallback(() => {
        if (subscriptionRef.current) {
            subscriptionRef.current.unsubscribe();
            subscriptionRef.current = null;
        }
        if (clientRef.current) {
            clientRef.current.deactivate();
            clientRef.current = null;
        }
    }, []);

    useEffect(() => {
        connect();
        return () => disconnect();
    }, [connect, disconnect]);

    return {
        connected: clientRef.current?.connected ?? false,
        disconnect,
        reconnect: () => {
            disconnect();
            connect();
        },
    };
}
