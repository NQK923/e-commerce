'use client';

import { useParams } from "next/navigation";
import React, { useCallback, useEffect, useState } from "react";
import { orderApi } from "@/src/api/orderApi";
import { Badge } from "@/src/components/ui/badge";
import { Button } from "@/src/components/ui/button";
import { Spinner } from "@/src/components/ui/spinner";
import { useAuth } from "@/src/store/auth-store";
import { useRequireAuth } from "@/src/hooks/use-require-auth";
import { Order } from "@/src/types/order";
import { formatCurrency, formatDate } from "@/src/utils/format";
import { useToast } from "@/src/components/ui/toast-provider";

export default function OrderDetailPage() {
  const params = useParams<{ id: string }>();
  const orderId = params?.id;
  const { isAuthenticated, initializing } = useRequireAuth();
  const { addToast } = useToast();
  const { user } = useAuth();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [working, setWorking] = useState(false);

  const [trackingNumber, setTrackingNumber] = useState("");
  const [trackingCarrier, setTrackingCarrier] = useState("");
  const [returnReason, setReturnReason] = useState("");
  const [returnNote, setReturnNote] = useState("");
  const [refundAmount, setRefundAmount] = useState("");

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

  const handleShip = async () => {
    if (!order) return;
    setWorking(true);
    try {
      const updated = await orderApi.ship(order.id, {
        trackingNumber,
        trackingCarrier,
      });
      setOrder(updated);
      addToast("Shipment started", "success");
    } catch (err) {
      addToast("Failed to start shipping", "error");
    } finally {
      setWorking(false);
    }
  };

  const handleMarkDelivered = async () => {
    if (!order) return;
    setWorking(true);
    try {
      const updated = await orderApi.markDelivered(order.id);
      setOrder(updated);
      addToast("Order marked as delivered", "success");
    } catch {
      addToast("Failed to mark delivered", "error");
    } finally {
      setWorking(false);
    }
  };

  const handleRequestReturn = async () => {
    if (!order || !user || !isOwner) return;
    setWorking(true);
    try {
      const updated = await orderApi.requestReturn(order.id, {
        userId: user.id,
        reason: returnReason,
        note: returnNote,
      });
      setOrder(updated);
      addToast("Return requested. We'll review it soon.", "success");
    } catch (err) {
      addToast("Unable to request a return", "error");
    } finally {
      setWorking(false);
    }
  };

  const handleApproveReturn = async () => {
    if (!order) return;
    setWorking(true);
    try {
      const updated = await orderApi.approveReturn(order.id, {
        refundAmount: refundAmount || undefined,
        currency: order.currency,
        note: returnNote,
      });
      setOrder(updated);
      addToast("Return approved and refund recorded", "success");
    } catch {
      addToast("Failed to approve return", "error");
    } finally {
      setWorking(false);
    }
  };

  const handleRejectReturn = async () => {
    if (!order) return;
    setWorking(true);
    try {
      const updated = await orderApi.rejectReturn(order.id, { note: returnNote });
      setOrder(updated);
      addToast("Return rejected", "success");
    } catch {
      addToast("Failed to reject return", "error");
    } finally {
      setWorking(false);
    }
  };

  useEffect(() => {
    if (initializing || !isAuthenticated) return;
    void loadOrder();
  }, [initializing, isAuthenticated, loadOrder]);

  useEffect(() => {
    if (order?.trackingNumber) {
      setTrackingNumber(order.trackingNumber);
    }
    if (order?.trackingCarrier) {
      setTrackingCarrier(order.trackingCarrier);
    }
  }, [order?.trackingCarrier, order?.trackingNumber]);

  const isAdmin = user?.roles?.includes("ADMIN");
  const isOwner = !!(user?.id && order?.userId && user.id === order.userId);

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
        <div className="flex items-center gap-2">
          <Badge>{order.status}</Badge>
          {order.returnStatus && order.returnStatus !== "NONE" ? (
            <Badge tone={order.returnStatus === "REQUESTED" ? "warning" : order.returnStatus === "APPROVED" ? "success" : "danger"}>
              Return: {order.returnStatus}
            </Badge>
          ) : null}
        </div>
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
            {order.refundAmount ? (
              <p className="text-sm text-zinc-600">
                Refund: {formatCurrency(order.refundAmount, order.currency ?? "USD")}
              </p>
            ) : null}
          </div>

          <div className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm space-y-2">
            <h3 className="text-lg font-semibold text-black">Shipment</h3>
            {order.trackingNumber ? (
              <div className="text-sm text-zinc-700 space-y-1">
                <p>Tracking: {order.trackingNumber}</p>
                {order.trackingCarrier ? <p>Carrier: {order.trackingCarrier}</p> : null}
                {order.shippedAt ? <p>Shipped at: {formatDate(order.shippedAt)}</p> : null}
                {order.deliveredAt ? <p>Delivered at: {formatDate(order.deliveredAt)}</p> : null}
              </div>
            ) : (
              <p className="text-sm text-zinc-600">No tracking assigned yet.</p>
            )}

            {isAdmin && order.status === "PAID" ? (
              <div className="space-y-2 pt-2 border-t border-zinc-100">
                <input
                  className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm"
                  placeholder="Tracking number"
                  value={trackingNumber}
                  onChange={(e) => setTrackingNumber(e.target.value)}
                />
                <input
                  className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm"
                  placeholder="Carrier (optional)"
                  value={trackingCarrier}
                  onChange={(e) => setTrackingCarrier(e.target.value)}
                />
                <Button onClick={handleShip} disabled={working || !trackingNumber}>
                  {working ? <Spinner className="mr-2 h-4 w-4" /> : null}
                  Start shipping
                </Button>
              </div>
            ) : null}

            {isAdmin && order.status === "SHIPPING" ? (
              <Button onClick={handleMarkDelivered} disabled={working}>
                {working ? <Spinner className="mr-2 h-4 w-4" /> : null}
                Mark delivered
              </Button>
            ) : null}
          </div>

          <div className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm space-y-2">
            <h3 className="text-lg font-semibold text-black">Returns & Refunds</h3>
            <div className="text-sm text-zinc-700 space-y-1">
              <p>Return status: {order.returnStatus ?? "NONE"}</p>
              {order.returnReason ? <p>Reason: {order.returnReason}</p> : null}
              {order.returnNote ? <p>Note: {order.returnNote}</p> : null}
            </div>

            {order.status === "DELIVERED" && (!order.returnStatus || order.returnStatus === "NONE") && isOwner ? (
              <div className="space-y-2 pt-2 border-t border-zinc-100">
                <textarea
                  className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm"
                  placeholder="Reason for return"
                  value={returnReason}
                  onChange={(e) => setReturnReason(e.target.value)}
                />
                <textarea
                  className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm"
                  placeholder="Additional notes (optional)"
                  value={returnNote}
                  onChange={(e) => setReturnNote(e.target.value)}
                />
                <Button onClick={handleRequestReturn} disabled={working}>
                  {working ? <Spinner className="mr-2 h-4 w-4" /> : null}
                  Request return
                </Button>
              </div>
            ) : null}

            {isAdmin && order.returnStatus === "REQUESTED" ? (
              <div className="space-y-2 pt-2 border-t border-zinc-100">
                <input
                  className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm"
                  placeholder={`Refund amount (${order.currency ?? "USD"})`}
                  value={refundAmount}
                  onChange={(e) => setRefundAmount(e.target.value)}
                />
                <textarea
                  className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm"
                  placeholder="Resolution note"
                  value={returnNote}
                  onChange={(e) => setReturnNote(e.target.value)}
                />
                <div className="flex gap-2">
                  <Button onClick={handleApproveReturn} disabled={working}>
                    {working ? <Spinner className="mr-2 h-4 w-4" /> : null}
                    Approve & refund
                  </Button>
                  <Button variant="secondary" onClick={handleRejectReturn} disabled={working}>
                    Reject
                  </Button>
                </div>
              </div>
            ) : null}
          </div>
        </div>
      </div>
    </div>
  );
}
