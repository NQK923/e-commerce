'use client';

import { Client, IMessage, StompSubscription } from "@stomp/stompjs";
import { useCallback, useEffect, useRef, useState } from "react";
import { config } from "../config/env";

export type ChatConnectionStatus = "idle" | "connecting" | "connected" | "error";

type UseChatClientOptions = {
  accessToken?: string | null;
  onMessage?: (message: unknown) => void;
  onAck?: (ack: unknown) => void;
  onError?: (error: Error) => void;
  onStatusChange?: (status: ChatConnectionStatus) => void;
};

type UseChatClientResult = {
  connected: boolean;
  status: ChatConnectionStatus;
  send: (payload: object) => void;
};

const toWebSocketUrl = (httpUrl: string): string => {
  if (httpUrl.startsWith("https://")) return `wss://${httpUrl.slice("https://".length)}`;
  if (httpUrl.startsWith("http://")) return `ws://${httpUrl.slice("http://".length)}`;
  return httpUrl;
};

export const useChatClient = ({
  accessToken,
  onMessage,
  onAck,
  onError,
  onStatusChange,
}: UseChatClientOptions): UseChatClientResult => {
  const clientRef = useRef<Client | null>(null);
  const subscriptionsRef = useRef<StompSubscription[]>([]);
  const onMessageRef = useRef<typeof onMessage | undefined>(undefined);
  const onAckRef = useRef<typeof onAck | undefined>(undefined);
  const onErrorRef = useRef<typeof onError | undefined>(undefined);
  const onStatusChangeRef = useRef<typeof onStatusChange | undefined>(undefined);
  const [status, setStatus] = useState<ChatConnectionStatus>("idle");

  useEffect(() => {
    onMessageRef.current = onMessage;
    onAckRef.current = onAck;
    onErrorRef.current = onError;
    onStatusChangeRef.current = onStatusChange;
  }, [onAck, onError, onMessage, onStatusChange]);

  useEffect(() => {
    const brokerURL = `${toWebSocketUrl(config.apiBaseUrl)}/ws/chat`;

    if (!accessToken) return;

    const client = new Client({
      brokerURL,
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      reconnectDelay: 5000,
      debug: () => {
        // Silence STOMP debug logs in production UI.
      },
    });

    client.onConnect = () => {
      setStatus("connected");
      onStatusChangeRef.current?.("connected");

      subscriptionsRef.current.push(
        client.subscribe("/user/queue/chat/messages", (frame: IMessage) => {
          try {
            const payload = JSON.parse(frame.body);
            onMessageRef.current?.(payload);
          } catch (err) {
            onErrorRef.current?.(err as Error);
          }
        }),
      );

      subscriptionsRef.current.push(
        client.subscribe("/user/queue/chat/ack", (frame: IMessage) => {
          try {
            const payload = JSON.parse(frame.body);
            onAckRef.current?.(payload);
          } catch (err) {
            onErrorRef.current?.(err as Error);
          }
        }),
      );
    };

    client.onStompError = () => {
      setStatus("error");
      onStatusChangeRef.current?.("error");
      onErrorRef.current?.(new Error("STOMP error"));
    };

    client.onWebSocketClose = () => {
      setStatus("idle");
      onStatusChangeRef.current?.("idle");
    };

    client.onWebSocketError = (event) => {
      setStatus("error");
      onStatusChangeRef.current?.("error");
      onErrorRef.current?.(new Error(`WebSocket error: ${event}`));
    };

    // Intentional: reflect connection attempt for UI.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setStatus("connecting");
    onStatusChangeRef.current?.("connecting");
    client.activate();
    clientRef.current = client;

    return () => {
      subscriptionsRef.current.forEach((sub) => sub.unsubscribe());
      subscriptionsRef.current = [];
      client.deactivate();
      clientRef.current = null;
      setStatus("idle");
      onStatusChangeRef.current?.("idle");
    };
  }, [accessToken]);

  useEffect(() => {
    if (accessToken) return;
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setStatus("idle");
    clientRef.current?.deactivate();
    clientRef.current = null;
    subscriptionsRef.current = [];
  }, [accessToken]);

  const send = useCallback(
    (payload: object) => {
      if (!clientRef.current || !clientRef.current.connected) {
        throw new Error("Chat connection is not ready");
      }
      clientRef.current.publish({
        destination: "/app/chat.send",
        body: JSON.stringify(payload),
      });
    },
    [],
  );

  return {
    connected: status === "connected",
    status,
    send,
  };
};
