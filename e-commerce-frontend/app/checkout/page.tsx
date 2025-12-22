'use client';

import { useRouter, useSearchParams } from "next/navigation";
import React, { Suspense, useEffect, useMemo, useState } from "react";
import { orderApi } from "@/src/api/orderApi";
import { CartSummary } from "@/src/components/cart/cart-summary";
import { Input } from "@/src/components/ui/input";
import { Spinner } from "@/src/components/ui/spinner";
import { useRequireAuth } from "@/src/hooks/use-require-auth";
import { useCart } from "@/src/store/cart-store";
import { useToast } from "@/src/components/ui/toast-provider";
import { useTranslation } from "@/src/providers/language-provider";
import { formatCurrency } from "@/src/utils/format";

function CheckoutContent() {
  const { user, isAuthenticated, initializing } = useRequireAuth();
  const router = useRouter();
  const searchParams = useSearchParams();
  const { cart, loading, clearCart, refreshCart } = useCart();
  const { addToast } = useToast();
  const { t } = useTranslation();
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

  const selectedParam = searchParams.get("selected");
  const selectedIds = useMemo(
    () => new Set((selectedParam ?? "").split(",").filter(Boolean)),
    [selectedParam],
  );

  const filteredCart = useMemo(() => {
    if (!cart) return cart;
    if (!selectedIds.size) return cart;
    const items = cart.items.filter((i) => selectedIds.has(i.id));
    const subtotal = items.reduce((sum, i) => sum + i.unitPrice * i.quantity, 0);
    const discount = cart.discountTotal ?? 0;
    const shipping = cart.shippingEstimate ?? 0;
    const total = subtotal + shipping - discount;
    return { ...cart, items, subtotal, total };
  }, [cart, selectedIds]);

  if (initializing || (loading && !cart) || !isAuthenticated) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        {t.checkout.preparing}
      </div>
    );
  }

  if (!filteredCart || filteredCart.items.length === 0) {
    return (
      <div className="mx-auto flex max-w-3xl flex-col items-center gap-3 px-4 py-10 text-center">
        <p className="text-lg font-semibold text-zinc-900">{t.cart.empty}</p>
        <p className="text-sm text-zinc-600">{t.cart.add_items_prompt}</p>
      </div>
    );
  }

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setSubmitting(true);
    try {
        const order = await orderApi.create({
          userId: user?.id,
          currency: filteredCart.currency ?? "USD",
          address,
          paymentMethod: "COD", // Placeholder as no UI for payment method yet
          items: filteredCart.items.map((item) => ({
            productId: item.product.id,
            variantSku: item.variantSku,
            quantity: item.quantity,
            price: item.unitPrice,
          })),
        });
      await clearCart();
      addToast(t.checkout.success, "success");
      router.replace(`/orders/${order.id}`);
    } catch (error) {
      const message = error instanceof Error ? error.message : t.checkout.failed;
      addToast(message, "error");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto grid max-w-6xl gap-8 px-4 py-10 lg:grid-cols-[2fr,1fr]">
      <div className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
        <h1 className="text-2xl font-bold text-zinc-900">{t.checkout.title}</h1>
        <p className="text-sm text-zinc-600">{t.checkout.subtitle}</p>
        <form className="mt-6 grid gap-4" onSubmit={handleSubmit}>
          <Input
            label={t.checkout.full_name}
            required
            value={address.fullName}
            onChange={(e) => setAddress((prev) => ({ ...prev, fullName: e.target.value }))}
          />
          <Input
            label={t.checkout.address_1}
            required
            value={address.line1}
            onChange={(e) => setAddress((prev) => ({ ...prev, line1: e.target.value }))}
          />
          <Input
            label={t.checkout.address_2}
            value={address.line2}
            onChange={(e) => setAddress((prev) => ({ ...prev, line2: e.target.value }))}
          />
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label={t.checkout.city}
              required
              value={address.city}
              onChange={(e) => setAddress((prev) => ({ ...prev, city: e.target.value }))}
            />
            <Input
              label={t.checkout.state}
              value={address.state}
              onChange={(e) => setAddress((prev) => ({ ...prev, state: e.target.value }))}
            />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label={t.checkout.postal_code}
              required
              value={address.postalCode}
              onChange={(e) => setAddress((prev) => ({ ...prev, postalCode: e.target.value }))}
            />
            <Input
              label={t.checkout.country}
              required
              value={address.country}
              onChange={(e) => setAddress((prev) => ({ ...prev, country: e.target.value }))}
            />
          </div>
            <button
              type="submit"
              disabled={submitting}
              className="rounded-lg bg-emerald-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-emerald-700 disabled:opacity-60 shadow-md w-full"
            >
              {submitting ? t.checkout.processing : `${t.checkout.place_order} - ${formatCurrency(filteredCart.total, filteredCart.currency)}`}
            </button>
        </form>
      </div>
      <CartSummary cart={filteredCart} actionLabel={t.checkout.place_order} />
    </div>
  );
}

export default function CheckoutPage() {
  return (
    <Suspense fallback={
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
      </div>
    }>
      <CheckoutContent />
    </Suspense>
  );
}
