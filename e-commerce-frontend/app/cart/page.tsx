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
import { useMemo, useReducer } from "react";
import { Cart } from "@/src/types/cart";

type SelectionAction =
  | { type: "sync"; items: Cart["items"] | undefined }
  | { type: "toggle"; id: string; value: boolean };

const selectionReducer = (state: Set<string>, action: SelectionAction) => {
  switch (action.type) {
    case "sync": {
      const incoming = new Set((action.items ?? []).map((i) => i.id));
      const next = new Set<string>();
      incoming.forEach((id) => next.add(id)); // auto-select new/remaining items
      // retain only items that still exist
      let changed = next.size !== state.size;
      if (!changed) {
        for (const id of state) {
          if (!next.has(id)) {
            changed = true;
            break;
          }
        }
      }
      return changed ? next : state;
    }
    case "toggle": {
      const next = new Set(state);
      if (action.value) {
        next.add(action.id);
      } else {
        next.delete(action.id);
      }
      return next;
    }
    default:
      return state;
  }
};

function CartContent() {
  const { isAuthenticated, initializing } = useRequireAuth();
  const { cart, loading, refreshCart, updateQuantity, removeItem, changeVariant } = useCart();
  const { addToast } = useToast();
  const router = useRouter();
  const { t } = useTranslation();
  const [selectedIds, dispatchSelection] = useReducer(selectionReducer, new Set<string>());

  useEffect(() => {
    if (!initializing && isAuthenticated) {
      void refreshCart();
    }
  }, [initializing, isAuthenticated, refreshCart]);

  useEffect(() => {
    dispatchSelection({ type: "sync", items: cart?.items });
  }, [cart?.items]);

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

  const handleVariantChange = async (itemId: string, newVariantSku: string) => {
    try {
      await changeVariant(itemId, newVariantSku);
      addToast("Variant updated", "success");
    } catch (error) {
        const message = error instanceof Error ? error.message : t.common.error;
        addToast(message, "error");
    }
  };

  const toggleSelect = (itemId: string, next: boolean) => {
    dispatchSelection({ type: "toggle", id: itemId, value: next });
  };

  const selectedCart = useMemo(() => {
    if (!cart) return cart;
    const items = cart.items.filter((i) => selectedIds.has(i.id));
    const subtotal = items.reduce((sum, i) => sum + i.unitPrice * i.quantity, 0);
    const discount = cart.discountTotal ?? 0;
    const shipping = cart.shippingEstimate ?? 0;
    const total = subtotal + shipping - discount;
    return { ...cart, items, subtotal, total };
  }, [cart, selectedIds]);

  const hasSelection = selectedIds.size > 0;

  if (initializing || (loading && !cart)) {
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
              selectable
              selected={selectedIds.has(item.id)}
              onSelectChange={(next) => toggleSelect(item.id, next)}
              onQuantityChange={(q) => handleUpdateQuantity(item.id, q)}
              onRemove={() => handleRemoveItem(item.id)}
              onVariantChange={(newSku) => handleVariantChange(item.id, newSku)}
            />
          ))}
        </div>
        <div>
          <CartSummary
            cart={selectedCart ?? cart}
            onCheckout={() => hasSelection && router.push(`/checkout?selected=${encodeURIComponent(Array.from(selectedIds).join(","))}`)}
            disableAction={!hasSelection}
            actionLabel={t.cart.proceed_checkout}
          />
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
