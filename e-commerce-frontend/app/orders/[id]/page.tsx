'use client';

import { useParams } from "next/navigation";
import React, { useCallback, useEffect, useState } from "react";
import { orderApi } from "@/src/api/orderApi";
import { Badge } from "@/src/components/ui/badge";
import { Button } from "@/src/components/ui/button";
import { Spinner } from "@/src/components/ui/spinner";
import { useRequireAuth } from "@/src/hooks/use-require-auth";
import { Order } from "@/src/types/order";
import { formatCurrency, formatDate } from "@/src/utils/format";
import { useToast } from "@/src/components/ui/toast-provider";

export default function OrderDetailPage() {
  const params = useParams<{ id: string }>();
  const orderId = params?.id;
  const { isAuthenticated, initializing } = useRequireAuth();
  const { addToast } = useToast();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadOrder = useCallback(async () => {
    if (!orderId) return;
    setLoading(true);
    try {
      const response = await orderApi.get(orderId);
      setOrder(response);
      setError(null);
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to load order";
      setError(message);
      addToast(message, "error");
    } finally {
      setLoading(false);
    }
  }, [addToast, orderId]);

  useEffect(() => {
    if (initializing || !isAuthenticated) return;
    void loadOrder();
  }, [initializing, isAuthenticated, loadOrder]);

  if (initializing || !isAuthenticated || loading) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        Loading order...
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="mx-auto flex max-w-3xl flex-col items-center gap-3 px-4 py-10 text-center">
        <p className="text-lg font-semibold text-black">Unable to load order.</p>
        <p className="text-sm text-zinc-600">{error}</p>
        <Button variant="secondary" onClick={loadOrder}>
          Retry
        </Button>
      </div>
    );
  }

  return (
    <div className="mx-auto flex max-w-5xl flex-col gap-6 px-4 py-10">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-black">Order #{order.id}</h1>
          <p className="text-sm text-zinc-600">Placed on {formatDate(order.createdAt)}</p>
        </div>
        <Badge tone="success">{order.status}</Badge>
      </div>

      <div className="grid gap-6 lg:grid-cols-[2fr,1fr]">
        <div className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
          <h2 className="text-lg font-semibold text-black">Items</h2>
          <div className="mt-4 space-y-3">
            {order.items.map((item) => (
              <div key={item.id} className="flex items-center justify-between rounded-xl border border-zinc-200 p-3">
                <div className="flex flex-col">
                  <span className="text-sm font-semibold text-black">Product {item.productId}</span>
                  <span className="text-xs text-zinc-500">Quantity: {item.quantity}</span>
                </div>
                <div className="text-right text-sm font-semibold text-black">
                  {formatCurrency(item.subtotal, order.currency ?? "USD")}
                  <div className="text-xs text-zinc-500">
                    {formatCurrency(item.price, order.currency ?? "USD")} each
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
        <div className="space-y-4">
          <div className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
            <h3 className="text-lg font-semibold text-black">Payment</h3>
            <p className="mt-2 text-sm text-zinc-600">Status: {order.status}</p>
            <p className="text-sm text-zinc-600">
              Total: {formatCurrency(order.total, order.currency ?? "USD")}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
