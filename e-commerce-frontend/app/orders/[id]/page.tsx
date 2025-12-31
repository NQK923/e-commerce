'use client';

import { useParams } from "next/navigation";
import React, { useCallback, useEffect, useState } from "react";
import { orderApi } from "@/src/api/orderApi";
import { productApi } from "@/src/api/productApi";
import { Badge } from "@/src/components/ui/badge";
import { Button } from "@/src/components/ui/button";
import { Spinner } from "@/src/components/ui/spinner";
import { useAuth } from "@/src/store/auth-store";
import { useRequireAuth } from "@/src/hooks/use-require-auth";
import { Order } from "@/src/types/order";
import { formatCurrency, formatDate } from "@/src/utils/format";
import { useToast } from "@/src/components/ui/toast-provider";
import { useTranslation } from "@/src/providers/language-provider";
import Image from "next/image";

export default function OrderDetailPage() {
  const params = useParams<{ id: string }>();
  const orderId = params?.id;
  const { isAuthenticated, initializing } = useRequireAuth();
  const { addToast } = useToast();
  const { t } = useTranslation();
  const { user } = useAuth();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [working, setWorking] = useState(false);
  const [productDetails, setProductDetails] = useState<Record<string, { name: string; image?: string; currency?: string }>>({});

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
      const message = err instanceof Error ? err.message : t.orders.load_failed;
      setError(message);
      addToast(message, "error");
    } finally {
      setLoading(false);
    }
  }, [addToast, orderId, t]);

  const handleShip = async () => {
    if (!order) return;
    setWorking(true);
    try {
      const updated = await orderApi.ship(order.id, {
        trackingNumber,
        trackingCarrier,
      });
      setOrder(updated);
      addToast(t.orders.shipment_started, "success");
    } catch {
      addToast(t.common.error, "error");
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
      addToast(t.orders.marked_delivered, "success");
    } catch {
      addToast(t.common.error, "error");
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
      addToast(t.orders.return_requested, "success");
    } catch {
      addToast(t.common.error, "error");
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
      addToast(t.orders.return_approved, "success");
    } catch {
      addToast(t.common.error, "error");
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
      addToast(t.orders.return_rejected, "success");
    } catch {
      addToast(t.common.error, "error");
    } finally {
      setWorking(false);
    }
  };

  useEffect(() => {
    if (initializing || !isAuthenticated) return;
    void loadOrder();
  }, [initializing, isAuthenticated, loadOrder]);

  useEffect(() => {
    if (!order) return;
    const uniqueProductIds = Array.from(new Set(order.items.map((i) => i.productId)));
    const loadProducts = async () => {
      const entries = await Promise.all(
        uniqueProductIds.map(async (id) => {
          try {
            const detail = await productApi.detail(id);
            return [id, { name: detail.name, image: detail.images?.[0]?.url, currency: detail.currency }] as const;
          } catch {
            return [id, null] as const;
          }
        }),
      );
      const mapped = entries.reduce<Record<string, { name: string; image?: string; currency?: string }>>((acc, [id, detail]) => {
        if (detail) acc[id] = detail;
        return acc;
      }, {});
      setProductDetails(mapped);
    };
    void loadProducts();
  }, [order]);

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
        {t.orders.loading}
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="mx-auto flex max-w-3xl flex-col items-center gap-3 px-4 py-10 text-center">
        <p className="text-lg font-semibold text-black">{t.orders.load_failed}</p>
        <p className="text-sm text-zinc-600">{error}</p>
        <Button variant="secondary" onClick={loadOrder}>
          {t.orders.retry}
        </Button>
      </div>
    );
  }

  return (
    <div className="mx-auto flex max-w-5xl flex-col gap-6 px-4 py-10">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-black">{t.orders.order_id.replace("{{id}}", order.id)}</h1>
          <p className="text-sm text-zinc-600">{t.orders.placed_on.replace("{{date}}", formatDate(order.createdAt))}</p>
        </div>
        <div className="flex items-center gap-2">
          <Badge>{order.status}</Badge>
          {order.returnStatus && order.returnStatus !== "NONE" ? (
            <Badge tone={order.returnStatus === "REQUESTED" ? "warning" : order.returnStatus === "APPROVED" ? "success" : "danger"}>
              {t.orders.return_status.replace("{{status}}", order.returnStatus)}
            </Badge>
          ) : null}
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-[2fr,1fr]">
        <div className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
          <h2 className="text-lg font-semibold text-black">{t.orders.items}</h2>
          <div className="mt-4 space-y-3">
            {order.items.map((item) => (
              <div key={item.id} className="flex items-center justify-between rounded-xl border border-zinc-200 p-3">
                <div className="flex items-center gap-3">
                  <div className="relative h-14 w-14 overflow-hidden rounded-lg border border-zinc-100 bg-zinc-50">
                    {productDetails[item.productId]?.image ? (
                      <Image
                        src={productDetails[item.productId]!.image!}
                        alt={productDetails[item.productId]?.name ?? item.productId}
                        fill
                        className="object-cover"
                      />
                    ) : (
                      <div className="flex h-full w-full items-center justify-center text-xs text-zinc-400">No image</div>
                    )}
                  </div>
                  <div className="flex flex-col">
                    <span className="text-sm font-semibold text-black">
                      {productDetails[item.productId]?.name ?? item.productId}
                    </span>
                    {item.variantSku ? (
                      <span className="text-xs text-zinc-500">SKU: {item.variantSku}</span>
                    ) : null}
                    <span className="text-xs text-zinc-500">
                      {t.orders.quantity}: {item.quantity}
                    </span>
                  </div>
                </div>
                <div className="text-right text-sm font-semibold text-black">
                  {formatCurrency(item.subtotal, order.currency ?? productDetails[item.productId]?.currency ?? "USD")}
                  <div className="text-xs text-zinc-500">
                    {formatCurrency(item.price, order.currency ?? productDetails[item.productId]?.currency ?? "USD")} {t.orders.each}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
        <div className="space-y-4">
          <div className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
            <h3 className="text-lg font-semibold text-black">{t.orders.payment}</h3>
            <p className="mt-2 text-sm text-zinc-600">{t.orders.status}: {order.status}</p>
            <p className="text-sm text-zinc-600">
              {t.orders.total}: {formatCurrency(order.total, order.currency ?? "USD")}
            </p>
            {order.refundAmount ? (
              <p className="text-sm text-zinc-600">
                {t.orders.refund}: {formatCurrency(order.refundAmount, order.currency ?? "USD")}
              </p>
            ) : null}
          </div>

          <div className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm space-y-2">
            <h3 className="text-lg font-semibold text-black">{t.orders.shipment}</h3>
            {order.trackingNumber ? (
              <div className="text-sm text-zinc-700 space-y-1">
                <p>{t.orders.tracking}: {order.trackingNumber}</p>
                {order.trackingCarrier ? <p>{t.orders.carrier}: {order.trackingCarrier}</p> : null}
                {order.shippedAt ? <p>{t.orders.shipped_at}: {formatDate(order.shippedAt)}</p> : null}
                {order.deliveredAt ? <p>{t.orders.delivered_at}: {formatDate(order.deliveredAt)}</p> : null}
              </div>
            ) : (
              <p className="text-sm text-zinc-600">{t.orders.no_tracking}</p>
            )}

            {isAdmin && order.status === "PAID" ? (
              <div className="space-y-2 pt-2 border-t border-zinc-100">
                <input
                  className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm"
                  placeholder={t.orders.tracking_number_placeholder}
                  value={trackingNumber}
                  onChange={(e) => setTrackingNumber(e.target.value)}
                />
                <input
                  className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm"
                  placeholder={t.orders.carrier_placeholder}
                  value={trackingCarrier}
                  onChange={(e) => setTrackingCarrier(e.target.value)}
                />
                <Button onClick={handleShip} disabled={working || !trackingNumber}>
                  {working ? <Spinner className="mr-2 h-4 w-4" /> : null}
                  {t.orders.start_shipping}
                </Button>
              </div>
            ) : null}

            {isAdmin && order.status === "SHIPPING" ? (
              <Button onClick={handleMarkDelivered} disabled={working}>
                {working ? <Spinner className="mr-2 h-4 w-4" /> : null}
                {t.orders.mark_delivered}
              </Button>
            ) : null}
          </div>

          <div className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm space-y-2">
            <h3 className="text-lg font-semibold text-black">{t.orders.returns_refunds}</h3>
            <div className="text-sm text-zinc-700 space-y-1">
              <p>{t.orders.return_status.replace("{{status}}", order.returnStatus ?? "NONE")}</p>
              {order.returnReason ? <p>{t.orders.reason}: {order.returnReason}</p> : null}
              {order.returnNote ? <p>{t.orders.note}: {order.returnNote}</p> : null}
            </div>

            {order.status === "DELIVERED" && (!order.returnStatus || order.returnStatus === "NONE") && isOwner ? (
              <div className="space-y-2 pt-2 border-t border-zinc-100">
                <textarea
                  className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm"
                  placeholder={t.orders.reason_placeholder}
                  value={returnReason}
                  onChange={(e) => setReturnReason(e.target.value)}
                />
                <textarea
                  className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm"
                  placeholder={t.orders.notes_placeholder}
                  value={returnNote}
                  onChange={(e) => setReturnNote(e.target.value)}
                />
                <Button onClick={handleRequestReturn} disabled={working}>
                  {working ? <Spinner className="mr-2 h-4 w-4" /> : null}
                  {t.orders.request_return}
                </Button>
              </div>
            ) : null}

            {isAdmin && order.returnStatus === "REQUESTED" ? (
              <div className="space-y-2 pt-2 border-t border-zinc-100">
                <input
                  className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm"
                  placeholder={t.orders.refund_amount_placeholder.replace("{{currency}}", order.currency ?? "USD")}
                  value={refundAmount}
                  onChange={(e) => setRefundAmount(e.target.value)}
                />
                <textarea
                  className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm"
                  placeholder={t.orders.resolution_note_placeholder}
                  value={returnNote}
                  onChange={(e) => setReturnNote(e.target.value)}
                />
                <div className="flex gap-2">
                  <Button onClick={handleApproveReturn} disabled={working}>
                    {working ? <Spinner className="mr-2 h-4 w-4" /> : null}
                    {t.orders.approve_refund}
                  </Button>
                  <Button variant="secondary" onClick={handleRejectReturn} disabled={working}>
                    {t.orders.reject}
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
