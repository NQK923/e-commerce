'use client';

import Link from "next/link";
import React from "react";
import { CartItemCard } from "@/src/components/cart/cart-item-card";
import { CartSummary } from "@/src/components/cart/cart-summary";
import { Spinner } from "@/src/components/ui/spinner";
import { useCart } from "@/src/store/cart-store";

export default function CartPage() {
  const { cart, loading, updateQuantity, removeItem } = useCart();

  if (loading && !cart) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        Loading cart...
      </div>
    );
  }

  return (
    <div className="mx-auto flex max-w-6xl flex-col gap-6 px-4 py-10">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-black">Your cart</h1>
          <p className="text-sm text-zinc-600">Review your items and continue to checkout.</p>
        </div>
        <Link href="/products" className="text-sm font-semibold text-black hover:underline">
          Continue shopping
        </Link>
      </div>

      {!cart || cart.items.length === 0 ? (
        <div className="rounded-2xl border border-dashed border-zinc-200 bg-white p-8 text-center text-sm text-zinc-600">
          Your cart is empty. Browse products to get started.
        </div>
      ) : (
        <div className="grid gap-6 lg:grid-cols-[2fr,1fr]">
          <div className="space-y-4">
            {cart.items.map((item) => (
              <CartItemCard
                key={item.id}
                item={item}
                onQuantityChange={(q) => updateQuantity(item.id, q)}
                onRemove={() => removeItem(item.id)}
              />
            ))}
          </div>
          <CartSummary cart={cart} onCheckout={() => (window.location.href = "/checkout")} />
        </div>
      )}
    </div>
  );
}
