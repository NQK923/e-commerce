'use client';

import Image from "next/image";
import Link from "next/link";
import React from "react";
import { CartItem } from "../../types/cart";
import { formatCurrency } from "../../utils/format";
import { Badge } from "../ui/badge";
import { Button } from "../ui/button";
import { QuantitySelector } from "./quantity-selector";
import { useTranslation } from "../../providers/language-provider";

type Props = {
  item: CartItem;
  onQuantityChange: (quantity: number) => void;
  onRemove: () => void;
};

export const CartItemCard: React.FC<Props> = ({ item, onQuantityChange, onRemove }) => {
  const { t } = useTranslation();
  const image = item.product.images.find((img) => img.primary) ?? item.product.images[0];
  return (
    <div className="flex gap-4 rounded-xl border border-zinc-200 bg-white p-4 shadow-sm">
      <div className="relative h-24 w-24 overflow-hidden rounded-lg bg-zinc-100">
        {image ? (
          <Image src={image.url} alt={image.altText ?? item.product.name} fill className="object-cover" sizes="100px" />
        ) : (
          <div className="flex h-full items-center justify-center text-xs text-zinc-500">No image</div>
        )}
      </div>
      <div className="flex flex-1 flex-col gap-2">
        <div className="flex items-start justify-between gap-3">
          <div className="flex flex-col gap-1">
            <Link href={`/products/${item.product.id}`} className="text-base font-semibold text-zinc-900 hover:text-emerald-600 hover:underline">
              {item.product.name}
            </Link>
            <div className="flex items-center gap-2">
              <span className="text-sm font-semibold">
                {formatCurrency(item.unitPrice, item.product.currency ?? "USD")}
              </span>
              {item.product.flashSaleEndAt && <Badge tone="warning">{t.product.flash_sale}</Badge>}
            </div>
          </div>
          <Button variant="ghost" size="sm" onClick={onRemove} className="text-sm text-zinc-600 hover:text-red-600">
            {t.cart.remove}
          </Button>
        </div>
        <div className="flex items-center justify-between">
          <QuantitySelector quantity={item.quantity} onChange={onQuantityChange} />
          <div className="text-right">
            <div className="text-sm text-zinc-600">{t.cart.item_subtotal}</div>
            <div className="text-lg font-bold text-emerald-700">
              {formatCurrency(item.subtotal, item.product.currency ?? "USD")}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
