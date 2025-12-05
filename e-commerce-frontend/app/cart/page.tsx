'use client';

import Link from "next/link";
import React, { Suspense, useEffect } from "react";
import { CartItemCard } from "@/src/components/cart/cart-item-card";
import { CartSummary } from "@/src/components/cart/cart-summary";
import { Spinner } from "@/src/components/ui/spinner";
import { useCart } from "@/src/store/cart-store";
import { useToast } from "@/src/components/ui/toast-provider";
import { useRequireAuth } from "@/src/hooks/use-require-auth";
import { useRouter } from "next/navigation";
import { useTranslation } from "@/src/providers/language-provider";

function CartContent() {
  const { isAuthenticated, initializing } = useRequireAuth();
  const { cart, loading, refreshCart, updateQuantity, removeItem } = useCart();
  const { addToast } = useToast();
  const router = useRouter();
  const { t } = useTranslation();

  useEffect(() => {
    if (!initializing && isAuthenticated) {
      void refreshCart();
    }
  }, [initializing, isAuthenticated, refreshCart]);

  const handleUpdateQuantity = async (itemId: string, quantity: number) => {
    try {
      await updateQuantity(itemId, quantity);
    } catch (error) {
      const message = error instanceof Error ? error.message : t.common.error;
      addToast(message, "error");
    }
  };

  const handleRemoveItem = async (itemId: string) => {
    try {
      await removeItem(itemId);
      addToast(t.common.success, "success");
    } catch (error) {
      const message = error instanceof Error ? error.message : t.common.error;
      addToast(message, "error");
    }
  };

  if (initializing || loading) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        {t.common.loading}
      </div>
    );
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="mx-auto flex max-w-3xl flex-col items-center gap-3 px-4 py-10 text-center">
        <p className="text-lg font-semibold text-zinc-900">{t.cart.empty}</p>
        <Link href="/products" className="text-sm font-semibold text-emerald-600 hover:underline">
          {t.cart.continue_shopping}
        </Link>
      </div>
    );
  }

  return (
    <div className="mx-auto flex max-w-6xl flex-col gap-8 px-4 py-10">
      <div className="flex items-center justify-between border-b border-zinc-200 pb-4">
        <h1 className="text-3xl font-bold text-zinc-900">{t.cart.title}</h1>
        <Link href="/products" className="text-sm font-semibold text-emerald-600 hover:underline">
          {t.cart.continue_shopping}
        </Link>
      </div>
      <div className="grid gap-8 lg:grid-cols-[2fr,1fr]">
        <div className="flex flex-col gap-4">
          {cart.items.map((item) => (
            <CartItemCard
              key={item.id}
              item={item}
              onQuantityChange={(q) => handleUpdateQuantity(item.id, q)}
              onRemove={() => handleRemoveItem(item.id)}
            />
          ))}
        </div>
        <div>
          <CartSummary cart={cart} onCheckout={() => router.push("/checkout")} />
        </div>
      </div>
    </div>
  );
}

export default function CartPage() {
  return (
    <Suspense fallback={
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        Loading cart...
      </div>
    }>
      <CartContent />
    </Suspense>
  );
}
