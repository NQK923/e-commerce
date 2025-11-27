'use client';

import React from "react";
import { Cart } from "../../types/cart";
import { formatCurrency } from "../../utils/format";
import { Button } from "../ui/button";

type Props = {
  cart: Cart;
  onCheckout?: () => void;
  actionLabel?: string;
};

export const CartSummary: React.FC<Props> = ({ cart, onCheckout, actionLabel = "Proceed to checkout" }) => {
  return (
    <div className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
      <h3 className="text-lg font-semibold text-black">Order summary</h3>
      <dl className="mt-4 space-y-2 text-sm text-zinc-700">
        <div className="flex justify-between">
          <dt>Subtotal</dt>
          <dd>{formatCurrency(cart.subtotal, cart.currency ?? "USD")}</dd>
        </div>
        {cart.discountTotal !== undefined && (
          <div className="flex justify-between">
            <dt>Discount</dt>
            <dd>-{formatCurrency(cart.discountTotal ?? 0, cart.currency ?? "USD")}</dd>
          </div>
        )}
        {cart.shippingEstimate !== undefined && (
          <div className="flex justify-between">
            <dt>Shipping</dt>
            <dd>{formatCurrency(cart.shippingEstimate ?? 0, cart.currency ?? "USD")}</dd>
          </div>
        )}
        <div className="flex justify-between text-base font-semibold text-black">
          <dt>Total</dt>
          <dd>{formatCurrency(cart.total, cart.currency ?? "USD")}</dd>
        </div>
      </dl>
      {onCheckout && (
        <Button className="mt-4 w-full" onClick={onCheckout}>
          {actionLabel}
        </Button>
      )}
    </div>
  );
};
