'use client';

import React from "react";
import { Cart } from "../../types/cart";
import { formatCurrency } from "../../utils/format";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { useTranslation } from "../../providers/language-provider";
import { Receipt, Truck, Tag, ArrowRight, ShieldCheck } from "lucide-react";

type Props = {
  cart: Cart;
  onCheckout?: () => void;
  actionLabel?: string;
  disableAction?: boolean;
};

export const CartSummary: React.FC<Props> = ({ cart, onCheckout, actionLabel, disableAction }) => {
  const { t } = useTranslation();
  const label = actionLabel || t.cart.proceed_checkout;
  const lowStockItems = (cart.items || []).filter(
    (item) => item.product.stock !== undefined && item.product.stock <= 5 && item.product.stock > 0,
  );

  return (
    <div className="sticky top-24 rounded-md border border-zinc-200 bg-white p-6 shadow-none">
      <h3 className="flex items-center gap-2 text-lg font-bold text-zinc-900 mb-6">
        <Receipt className="h-5 w-5 text-black" />
        {t.cart.summary_title}
      </h3>

      {lowStockItems.length > 0 && (
        <div className="mb-4 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs font-semibold text-amber-700">
          {t.cart.low_stock_warning.replace("{{items}}", lowStockItems.map((i) => `${i.product.stock}x ${i.product.name}`).join(", "))}
        </div>
      )}

      <div className="space-y-4">
        {/* Coupon Input */}
        <div className="flex gap-2">
            <div className="relative flex-1">
                <Tag className="absolute left-2.5 top-2.5 h-4 w-4 text-zinc-400" />
                <Input placeholder={t.cart.coupon_placeholder} className="pl-9 bg-transparent border-zinc-300 focus-visible:ring-black rounded-md" />
            </div>
            <Button variant="outline" size="sm" className="shrink-0 text-black border-zinc-300 hover:bg-zinc-100 rounded-md">
                {t.cart.apply}
            </Button>
        </div>

        <div className="h-px bg-zinc-100 my-4" />

        <dl className="space-y-3 text-sm text-zinc-600">
            <div className="flex justify-between items-center">
            <dt>{t.cart.subtotal}</dt>
            <dd className="font-medium text-zinc-900">{formatCurrency(cart.subtotal, cart.currency ?? "USD")}</dd>
            </div>
            
            {cart.discountTotal !== undefined && cart.discountTotal > 0 && (
            <div className="flex justify-between items-center text-zinc-900">
                <dt className="flex items-center gap-1.5"><Tag size={14} /> {t.cart.discount}</dt>
                <dd>-{formatCurrency(cart.discountTotal ?? 0, cart.currency ?? "USD")}</dd>
            </div>
            )}
            
            <div className="flex justify-between items-center text-zinc-500">
            <dt className="flex items-center gap-1.5"><Truck size={14} /> {t.cart.shipping_estimate}</dt>
            <dd>{cart.shippingEstimate ? formatCurrency(cart.shippingEstimate, cart.currency ?? "USD") : t.cart.shipping_pending}</dd>
            </div>

            <div className="h-px bg-zinc-100 my-2" />
            
            <div className="flex justify-between items-end pt-2">
            <dt className="text-base font-bold text-zinc-900">{t.cart.total}</dt>
            <dd className="text-2xl font-bold text-black">
                {formatCurrency(cart.total, cart.currency ?? "USD")}
            </dd>
            </div>
            <div className="text-xs text-right text-zinc-400 mt-1">
                {t.cart.vat_included}
            </div>
        </dl>
      </div>

      {onCheckout && (
        <div className="mt-8 space-y-4">
            <Button
            className="w-full h-14 text-sm font-bold uppercase tracking-widest bg-black hover:bg-zinc-800 text-white rounded-md disabled:opacity-60 transition-colors"
            onClick={onCheckout}
            disabled={disableAction}
            >
            {label}
            <ArrowRight className="ml-2 h-4 w-4" />
            </Button>
            
            <div className="flex items-center justify-center gap-2 text-xs text-zinc-500 bg-transparent py-2 rounded-md border border-zinc-100 mt-2">
                <ShieldCheck size={14} className="text-black" />
                {t.cart.secure_payment}
            </div>
        </div>
      )}
    </div>
  );
};
