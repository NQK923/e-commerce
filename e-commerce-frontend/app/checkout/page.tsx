'use client';

import { useRouter } from "next/navigation";
import React, { useEffect, useState } from "react";
import { orderApi } from "../../src/api/orderApi";
import { CartSummary } from "../../src/components/cart/cart-summary";
import { Input } from "../../src/components/ui/input";
import { Spinner } from "../../src/components/ui/spinner";
import { useRequireAuth } from "../../src/hooks/use-require-auth";
import { useCart } from "../../src/store/cart-store";
import { useToast } from "../../src/components/ui/toast-provider";

export default function CheckoutPage() {
  const { isAuthenticated, initializing } = useRequireAuth();
  const router = useRouter();
  const { cart, loading, clearCart, refreshCart } = useCart();
  const { addToast } = useToast();
  const [submitting, setSubmitting] = useState(false);
  const [address, setAddress] = useState({
    fullName: "",
    line1: "",
    line2: "",
    city: "",
    state: "",
    postalCode: "",
    country: "",
  });

  useEffect(() => {
    if (!initializing && isAuthenticated) {
      void refreshCart();
    }
  }, [initializing, isAuthenticated, refreshCart]);

  if (initializing || loading || !isAuthenticated) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        Preparing checkout...
      </div>
    );
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="mx-auto flex max-w-3xl flex-col items-center gap-3 px-4 py-10 text-center">
        <p className="text-lg font-semibold text-black">Your cart is empty</p>
        <p className="text-sm text-zinc-600">Add items before checking out.</p>
      </div>
    );
  }

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setSubmitting(true);
    try {
      const order = await orderApi.create({ address });
      await clearCart();
      addToast("Order placed successfully", "success");
      router.replace(`/orders/${order.id}`);
    } catch (error) {
      const message = error instanceof Error ? error.message : "Checkout failed";
      addToast(message, "error");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto grid max-w-6xl gap-8 px-4 py-10 lg:grid-cols-[2fr,1fr]">
      <div className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
        <h1 className="text-2xl font-bold text-black">Checkout</h1>
        <p className="text-sm text-zinc-600">Provide your shipping details to place the order.</p>
        <form className="mt-6 grid gap-4" onSubmit={handleSubmit}>
          <Input
            label="Full name"
            required
            value={address.fullName}
            onChange={(e) => setAddress((prev) => ({ ...prev, fullName: e.target.value }))}
          />
          <Input
            label="Address line 1"
            required
            value={address.line1}
            onChange={(e) => setAddress((prev) => ({ ...prev, line1: e.target.value }))}
          />
          <Input
            label="Address line 2"
            value={address.line2}
            onChange={(e) => setAddress((prev) => ({ ...prev, line2: e.target.value }))}
          />
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label="City"
              required
              value={address.city}
              onChange={(e) => setAddress((prev) => ({ ...prev, city: e.target.value }))}
            />
            <Input
              label="State/Province"
              value={address.state}
              onChange={(e) => setAddress((prev) => ({ ...prev, state: e.target.value }))}
            />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label="Postal code"
              required
              value={address.postalCode}
              onChange={(e) => setAddress((prev) => ({ ...prev, postalCode: e.target.value }))}
            />
            <Input
              label="Country"
              required
              value={address.country}
              onChange={(e) => setAddress((prev) => ({ ...prev, country: e.target.value }))}
            />
          </div>
          <button
            type="submit"
            className="rounded-lg bg-black px-4 py-3 text-sm font-semibold text-white transition hover:bg-zinc-800 disabled:opacity-60"
            disabled={submitting}
          >
            {submitting ? "Placing order..." : "Place order"}
          </button>
        </form>
      </div>
      <CartSummary cart={cart} />
    </div>
  );
}
