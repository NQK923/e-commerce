'use client';

import { useEffect, useRef, useCallback, useState } from 'react';
import { Client, StompSubscription } from '@stomp/stompjs';
import { useAuth } from '@/src/store/auth-store';
import { config } from '@/src/config/env';

export interface Notification {
    id: string;
    userId: string;
    type: string;
    title: string;
    message: string;
    read: boolean;
    createdAt: string;
    metadata?: Record<string, unknown>;
}

interface UseNotificationSocketOptions {
    onNotification?: (notification: Notification) => void;
    enabled?: boolean;
}

const toWebSocketUrl = (httpUrl: string): string => {
    if (httpUrl.startsWith('https://')) return `wss://${httpUrl.slice('https://'.length)}`;
    if (httpUrl.startsWith('http://')) return `ws://${httpUrl.slice('http://'.length)}`;
    return httpUrl;
};

/**
 * Hook to subscribe to real-time notifications via WebSocket
 * Reuses the backend STOMP endpoint shared with chat.
 */
export function useNotificationSocket({
    onNotification,
    enabled = true
}: UseNotificationSocketOptions = {}) {
    const { user, accessToken: token } = useAuth();
    const clientRef = useRef<Client | null>(null);
    const subscriptionRef = useRef<StompSubscription | null>(null);
    const [connected, setConnected] = useState(false);

    const connect = useCallback(() => {
        if (!user || !token || !enabled) return;

        const brokerURL = `${toWebSocketUrl(config.apiBaseUrl)}/ws/chat`;

        const client = new Client({
            brokerURL,
            connectHeaders: {
                Authorization: `Bearer ${token}`,
            },
            debug: (str) => {
                if (process.env.NODE_ENV === 'development') {
                    console.log('[Notification WebSocket]', str);
                }
            },
            onConnect: () => {
                setConnected(true);

                // Subscribe to user's notification queue
                const subscription = client.subscribe(
                    '/user/queue/notifications',
                    (message) => {
                        try {
                            const notification: Notification = JSON.parse(message.body);
                            onNotification?.(notification);
                        } catch (error) {
                            if (process.env.NODE_ENV === 'development') {
                                console.error('[Notification] Failed to parse notification:', error);
                            }
                        }
                    }
                );

                subscriptionRef.current = subscription;
            },
            onDisconnect: () => {
                setConnected(false);
            },
            onWebSocketClose: () => {
                setConnected(false);
            },
            onStompError: (frame) => {
                if (process.env.NODE_ENV === 'development') {
                    console.error('[Notification] STOMP error:', frame);
                }
                setConnected(false);
            },
            onWebSocketError: (event) => {
                if (process.env.NODE_ENV === 'development') {
                    console.error('[Notification] WebSocket error:', event);
                }
                setConnected(false);
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
        setConnected(false);
    }, []);

    useEffect(() => {
        connect();
        return () => disconnect();
    }, [connect, disconnect]);

    return {
        connected,
        disconnect,
        reconnect: () => {
            disconnect();
            connect();
        },
    };
}
