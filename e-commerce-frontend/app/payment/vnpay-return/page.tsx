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
  const [verifying, setVerifying] = useState(true);

  useEffect(() => {
    const verify = async () => {
      try {
        const query = searchParams.toString();
        if (!query) {
            throw new Error("Missing payment parameters");
        }
        
        const order = await orderApi.verifyPayment(query);
        addToast(t.checkout.success, "success");
        router.replace(`/orders/${order.id}`);
      } catch (error) {
        console.error(error);
        const message = error instanceof Error ? error.message : "Payment verification failed";
        addToast(message, "error");
        // Redirect to home or orders list on failure after a delay, or show a try again button
        // For now, let's go to orders
        setTimeout(() => router.replace("/orders"), 3000);
      } finally {
        setVerifying(false);
      }
    };

    verify();
  }, [searchParams, addToast, router, t]);

  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center gap-4 text-center">
      {verifying ? (
        <>
          <Spinner size="lg" />
          <p className="text-zinc-600">Verifying your payment...</p>
        </>
      ) : (
        <p className="text-zinc-600">Redirecting...</p>
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
