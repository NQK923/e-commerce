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
import { ChevronDown } from "lucide-react";
import { Checkbox } from "../ui/checkbox";

type Props = {
  item: CartItem;
  onQuantityChange: (quantity: number) => void;
  onRemove: () => void;
  onVariantChange?: (newSku: string) => void;
  selectable?: boolean;
  selected?: boolean;
  onSelectChange?: (next: boolean) => void;
};

export const CartItemCard: React.FC<Props> = ({ item, onQuantityChange, onRemove, onVariantChange, selectable, selected, onSelectChange }) => {
  const { t } = useTranslation();
  const image = item.product.images.find((img) => img.primary) ?? item.product.images[0];
  const maxQty = item.product.stock ?? 99;
  
  const hasVariants = item.product.variants && item.product.variants.length > 0;
  
  // Use local state for immediate feedback if needed, but here we rely on props
  const currentVariantSku = item.variantSku;

  const handleVariantChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
      if (onVariantChange) {
          onVariantChange(e.target.value);
      }
  };

  return (
    <div className="flex flex-col sm:flex-row gap-4 rounded-xl border border-zinc-200 bg-white p-4 shadow-sm hover:shadow-md transition-shadow">
      {/* Image */}
      <div className="flex items-start gap-3">
        {selectable && (
          <Checkbox
            checked={selected}
            onCheckedChange={(v) => onSelectChange?.(!!v)}
            className="mt-1"
          />
        )}
        <div className="relative h-24 w-24 shrink-0 overflow-hidden rounded-lg bg-zinc-100 border border-zinc-100">
          {image ? (
            <Image src={image.url} alt={image.altText ?? item.product.name} fill className="object-cover" sizes="100px" />
          ) : (
            <div className="flex h-full items-center justify-center text-xs text-zinc-500">No image</div>
          )}
        </div>
      </div>

      {/* Content */}
      <div className="flex flex-1 flex-col justify-between gap-4">
        <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-2">
          <div className="space-y-1">
            <Link href={`/products/${item.product.id}`} className="text-base font-semibold text-zinc-900 hover:text-emerald-600 hover:underline line-clamp-2">
              {item.product.name}
            </Link>
            
            <div className="flex flex-wrap items-center gap-2">
               {/* Variant Selector */}
                {hasVariants ? (
                    <div className="relative inline-block">
                        <select 
                            value={currentVariantSku || ''} 
                            onChange={handleVariantChange}
                            disabled={!onVariantChange}
                            className="appearance-none cursor-pointer rounded-md border border-zinc-300 bg-white py-1 pl-2 pr-8 text-xs font-medium text-zinc-700 shadow-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500 hover:bg-zinc-50"
                        >
                            {item.product.variants?.map((v) => (
                                <option key={v.sku} value={v.sku}>
                                    {v.name}
                                </option>
                            ))}
                        </select>
                         <ChevronDown size={14} className="pointer-events-none absolute right-2 top-1/2 -translate-y-1/2 text-zinc-500" />
                    </div>
                ) : (
                    <span className="text-xs text-zinc-500">Mặc định</span>
                )}
            </div>

            <div className="flex items-center gap-2 mt-1">
              <span className="text-sm font-semibold text-emerald-600">
                {formatCurrency(item.unitPrice, item.product.currency ?? "USD")}
              </span>
              {item.product.flashSaleEndAt && <Badge tone="warning" className="text-[10px] px-1 py-0">{t.product.flash_sale}</Badge>}
            </div>
          </div>

          <Button variant="ghost" size="sm" onClick={onRemove} className="self-start text-xs text-zinc-400 hover:text-red-600 hover:bg-red-50 -mt-1 -mr-2">
            {t.cart.remove}
          </Button>
        </div>

        <div className="flex items-center justify-between border-t border-zinc-100 pt-3 mt-auto">
          <QuantitySelector quantity={item.quantity} onChange={onQuantityChange} max={maxQty} size="sm" />
          <div className="text-right">
            {/* <div className="text-[10px] text-zinc-500 uppercase font-medium tracking-wide">{t.cart.item_subtotal}</div> */}
            <div className="text-base font-bold text-zinc-900">
              {formatCurrency(item.subtotal, item.product.currency ?? "USD")}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
