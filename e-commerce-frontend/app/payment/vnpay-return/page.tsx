'use client';

import { useRouter, useSearchParams } from "next/navigation";
import React, { Suspense, useEffect, useState } from "react";
import { orderApi } from "@/src/api/orderApi";
import { Spinner } from "@/src/components/ui/spinner";
import { useToast } from "@/src/components/ui/toast-provider";
import { useTranslation } from "@/src/providers/language-provider";

function PaymentReturnContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { addToast } = useToast();
  const { t } = useTranslation();
  const [verifying, setVerifying] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [orderId, setOrderId] = useState<string | null>(null);

  const verify = async () => {
    setVerifying(true);
    setError(null);
    try {
      const query = searchParams.toString();
      if (!query) {
        throw new Error("Missing payment parameters");
      }
      const order = await orderApi.verifyPayment(query);
      setOrderId(order.id);
      addToast(t.checkout.success, "success");
      // Give the user a quick success state before redirect.
      setTimeout(() => router.replace(`/orders/${order.id}`), 500);
    } catch (err) {
      console.error(err);
      const message = err instanceof Error ? err.message : "Payment verification failed";
      setError(message);
      addToast(message, "error");
    } finally {
      setVerifying(false);
    }
  };

  useEffect(() => {
    void verify();
  }, []); // run once on mount

  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center gap-4 text-center">
      {verifying ? (
        <>
          <Spinner size="lg" />
          <p className="text-zinc-600">{t.checkout.preparing}</p>
        </>
      ) : error ? (
        <div className="space-y-3 max-w-md">
          <p className="text-lg font-semibold text-rose-700">{t.checkout.failed}</p>
          <p className="text-sm text-zinc-600">{error}</p>
          <div className="flex flex-wrap justify-center gap-3">
            <button
              className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-semibold text-white"
              onClick={() => void verify()}
            >
              {t.common.retry}
            </button>
            <button
              className="rounded-lg border border-zinc-200 px-4 py-2 text-sm font-semibold text-zinc-700 hover:bg-zinc-50"
              onClick={() => router.replace("/orders")}
            >
              {t.orders.title}
            </button>
          </div>
        </div>
      ) : (
        <div className="space-y-2">
          <p className="text-lg font-semibold text-emerald-700">{t.checkout.success}</p>
          <p className="text-sm text-zinc-600">
            {orderId ? t.orders.order_id.replace("{{id}}", orderId) : t.checkout.preparing}
          </p>
          <p className="text-xs text-zinc-500">Redirecting to your order...</p>
        </div>
      )}
    </div>
  );
}

export default function PaymentReturnPage() {
  return (
    <Suspense fallback={
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
      </div>
    }>
      <PaymentReturnContent />
    </Suspense>
  );
}
