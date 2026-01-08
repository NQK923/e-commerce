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
import { ChevronDown, Trash2 } from "lucide-react";
import { Checkbox } from "../ui/checkbox";

type Props = {
  item: CartItem;
  onQuantityChange: (quantity: number) => void;
  onRemove: () => void;
  onVariantChange?: (newSku: string) => void;
  selectable?: boolean;
  selected?: boolean;
  onSelectChange?: (next: boolean) => void;
  eager?: boolean;
};

export const CartItemCard: React.FC<Props> = ({ item, onQuantityChange, onRemove, onVariantChange, selectable, selected, onSelectChange, eager }) => {
  const { t } = useTranslation();
  const image = item.product.images.find((img) => img.primary) ?? item.product.images[0];
  const maxQty = item.product.stock ?? 99;
  
  const hasVariants = item.product.variants && item.product.variants.length > 0;
  
  const currentVariantSku = item.variantSku;

  const handleVariantChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
      if (onVariantChange) {
          onVariantChange(e.target.value);
      }
  };

  return (
    <div className="group relative flex flex-col sm:flex-row gap-5 rounded-2xl border border-zinc-100 bg-white p-4 shadow-sm transition-all hover:shadow-md hover:border-emerald-100">
      {/* Selection & Image */}
      <div className="flex items-start gap-4">
        {selectable && (
          <div className="pt-8">
             <Checkbox
                checked={selected}
                onCheckedChange={(v) => onSelectChange?.(!!v)}
                className="h-5 w-5 border-zinc-300 data-[state=checked]:bg-emerald-600 data-[state=checked]:border-emerald-600"
              />
          </div>
        )}
        <Link href={`/products/${item.product.id}`} className="relative h-28 w-24 shrink-0 overflow-hidden rounded-xl bg-zinc-50 border border-zinc-100 group-hover:border-emerald-200 transition-colors">
          {image ? (
            <Image
              src={image.url}
              alt={image.altText ?? item.product.name}
              fill
              priority={eager}
              loading={eager ? "eager" : undefined}
              className="object-cover"
              sizes="100px"
            />
          ) : (
            <div className="flex h-full items-center justify-center text-xs text-zinc-400">No image</div>
          )}
        </Link>
      </div>

      {/* Content */}
      <div className="flex flex-1 flex-col justify-between gap-3 min-w-0">
        <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-2">
          <div className="space-y-1.5 pr-8">
            <Link href={`/products/${item.product.id}`} className="text-base font-medium text-zinc-900 hover:text-emerald-700 transition-colors line-clamp-2" title={item.product.name}>
              {item.product.name}
            </Link>
            
            <div className="flex flex-wrap items-center gap-2">
               {/* Variant Selector */}
                {hasVariants ? (
                    <div className="relative inline-block group/select">
                        <select 
                            value={currentVariantSku || ''} 
                            onChange={handleVariantChange}
                            disabled={!onVariantChange}
                            className="appearance-none cursor-pointer rounded-lg bg-zinc-50 border border-zinc-200 py-1 pl-2.5 pr-8 text-xs font-medium text-zinc-600 transition-colors hover:bg-zinc-100 hover:border-zinc-300 focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-100"
                        >
                            {item.product.variants?.map((v) => (
                                <option key={v.sku} value={v.sku}>
                                    {v.name}
                                </option>
                            ))}
                        </select>
                         <ChevronDown size={14} className="pointer-events-none absolute right-2 top-1/2 -translate-y-1/2 text-zinc-400 group-hover/select:text-zinc-600" />
                    </div>
                ) : (
                   null
                )}
                 {item.product.flashSaleEndAt && <Badge className="bg-amber-100 text-amber-700 hover:bg-amber-200 border-0 text-[10px] px-2 py-0.5">{t.product.flash_sale}</Badge>}
            </div>
          </div>
          
           {/* Remove Button for Desktop - Absolute top right */}
           <button 
             onClick={onRemove} 
             className="absolute top-4 right-4 hidden sm:flex text-zinc-400 hover:text-rose-500 transition-colors p-1 rounded-md hover:bg-rose-50"
             title={t.cart.remove}
           >
             <Trash2 size={18} />
           </button>
        </div>

        <div className="flex flex-wrap items-end justify-between gap-4 mt-2">
          <div className="flex items-center gap-3">
             <div className="flex flex-col">
                 {/* Unit Price */}
                 <span className="text-xs text-zinc-500 font-medium">Đơn giá</span>
                 <span className="text-sm font-semibold text-zinc-700">
                    {formatCurrency(item.unitPrice, item.product.currency ?? "USD")}
                 </span>
             </div>
             <div className="h-8 w-px bg-zinc-100 mx-1 hidden xs:block"></div>
             <QuantitySelector quantity={item.quantity} onChange={onQuantityChange} max={maxQty} size="sm" />
          </div>

          <div className="flex flex-col items-end">
             {/* Subtotal */}
             <span className="text-xs text-zinc-500 font-medium">Thành tiền</span>
             <span className="text-lg font-bold text-emerald-700">
               {formatCurrency(item.subtotal, item.product.currency ?? "USD")}
             </span>
          </div>
        </div>
        
         {/* Remove Button for Mobile */}
         <Button variant="ghost" size="sm" onClick={onRemove} className="sm:hidden self-start text-xs text-red-500 hover:bg-red-50 h-8 px-0 -ml-2">
            <Trash2 size={14} className="mr-1.5" />
            {t.cart.remove}
         </Button>
      </div>
    </div>
  );
};
