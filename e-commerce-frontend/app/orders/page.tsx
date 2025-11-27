'use client';

import React, { useCallback, useEffect, useState } from "react";
import { orderApi } from "../../src/api/orderApi";
import { OrderCard } from "../../src/components/orders/order-card";
import { Button } from "../../src/components/ui/button";
import { Spinner } from "../../src/components/ui/spinner";
import { useRequireAuth } from "../../src/hooks/use-require-auth";
import { Order } from "../../src/types/order";
import { useToast } from "../../src/components/ui/toast-provider";

export default function OrdersPage() {
  const { isAuthenticated, initializing } = useRequireAuth();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { addToast } = useToast();

  const loadOrders = useCallback(async () => {
    setLoading(true);
    try {
      const response = await orderApi.list(0, 20);
      setOrders(response.items);
      setError(null);
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to load orders";
      setError(message);
      addToast(message, "error");
    } finally {
      setLoading(false);
    }
  }, [addToast]);

  useEffect(() => {
    if (initializing || !isAuthenticated) return;
    void loadOrders();
  }, [initializing, isAuthenticated, loadOrders]);

  if (initializing || !isAuthenticated) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        Loading orders...
      </div>
    );
  }

  return (
    <div className="mx-auto flex max-w-6xl flex-col gap-6 px-4 py-10">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-black">Orders</h1>
          <p className="text-sm text-zinc-600">Track your order history and status.</p>
        </div>
        <Button variant="secondary" onClick={loadOrders} disabled={loading}>
          Refresh
        </Button>
      </div>

      {loading && (
        <div className="flex items-center gap-3 text-sm text-zinc-600">
          <Spinner />
          Loading orders...
        </div>
      )}

      {error && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">{error}</div>
      )}

      <div className="space-y-3">
        {orders.map((order) => (
          <OrderCard key={order.id} order={order} />
        ))}
        {!orders.length && !loading && (
          <div className="rounded-xl border border-dashed border-zinc-200 bg-white p-6 text-sm text-zinc-600">
            No orders yet. Place your first order to see it here.
          </div>
        )}
      </div>
    </div>
  );
}
