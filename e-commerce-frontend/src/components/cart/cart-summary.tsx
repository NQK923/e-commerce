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

  return (
    <div className="sticky top-24 rounded-2xl border border-zinc-100 bg-white p-6 shadow-lg shadow-zinc-200/50">
      <h3 className="flex items-center gap-2 text-lg font-bold text-zinc-900 mb-6">
        <Receipt className="h-5 w-5 text-emerald-600" />
        {t.cart.summary_title}
      </h3>

      <div className="space-y-4">
        {/* Coupon Input */}
        <div className="flex gap-2">
            <div className="relative flex-1">
                <Tag className="absolute left-2.5 top-2.5 h-4 w-4 text-zinc-400" />
                <Input placeholder="Mã giảm giá" className="pl-9 bg-zinc-50 border-zinc-200 focus-visible:ring-emerald-500" />
            </div>
            <Button variant="outline" size="sm" className="shrink-0 text-emerald-700 border-emerald-200 hover:bg-emerald-50">
                Áp dụng
            </Button>
        </div>

        <div className="h-px bg-zinc-100 my-4" />

        <dl className="space-y-3 text-sm text-zinc-600">
            <div className="flex justify-between items-center">
            <dt>{t.cart.subtotal}</dt>
            <dd className="font-medium text-zinc-900">{formatCurrency(cart.subtotal, cart.currency ?? "USD")}</dd>
            </div>
            
            {cart.discountTotal !== undefined && cart.discountTotal > 0 && (
            <div className="flex justify-between items-center text-emerald-600">
                <dt className="flex items-center gap-1.5"><Tag size={14} /> Giảm giá</dt>
                <dd>-{formatCurrency(cart.discountTotal ?? 0, cart.currency ?? "USD")}</dd>
            </div>
            )}
            
            <div className="flex justify-between items-center text-zinc-500">
            <dt className="flex items-center gap-1.5"><Truck size={14} /> {t.cart.shipping_estimate}</dt>
            <dd>{cart.shippingEstimate ? formatCurrency(cart.shippingEstimate, cart.currency ?? "USD") : "Chưa tính"}</dd>
            </div>

            <div className="h-px bg-zinc-100 my-2" />
            
            <div className="flex justify-between items-end pt-2">
            <dt className="text-base font-bold text-zinc-900">{t.cart.total}</dt>
            <dd className="text-2xl font-bold text-emerald-700">
                {formatCurrency(cart.total, cart.currency ?? "USD")}
            </dd>
            </div>
            <div className="text-xs text-right text-zinc-400 mt-1">
                (Đã bao gồm VAT nếu có)
            </div>
        </dl>
      </div>

      {onCheckout && (
        <div className="mt-8 space-y-4">
            <Button
            className="w-full h-12 text-base bg-emerald-600 hover:bg-emerald-700 disabled:opacity-60 shadow-lg shadow-emerald-600/20 transition-all hover:-translate-y-0.5"
            onClick={onCheckout}
            disabled={disableAction}
            >
            {label}
            <ArrowRight className="ml-2 h-4 w-4" />
            </Button>
            
            <div className="flex items-center justify-center gap-2 text-xs text-zinc-400 bg-zinc-50 py-2 rounded-lg">
                <ShieldCheck size={14} className="text-emerald-500" />
                Bảo mật thanh toán 100%
            </div>
        </div>
      )}
    </div>
  );
};
