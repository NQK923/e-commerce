'use client';

import Image from "next/image";
import Link from "next/link";
import React from "react";
import { Product } from "../../types/product";
import { formatCurrency, formatNumber } from "../../utils/format";
import { Badge } from "../ui/badge";
import { Card } from "../ui/card";
import { Button } from "../ui/button";
import { useTranslation } from "../../providers/language-provider";
import { ShoppingCart, Star } from "lucide-react";
import { useCart } from "../../store/cart-store";
import { useToast } from "../ui/toast-provider";

type Props = {
  product: Product;
};

export const ProductCard: React.FC<Props> = ({ product }) => {
  const { t } = useTranslation();
  const { addItem } = useCart();
  const { addToast } = useToast();
  const primaryImage = product.images.find((img) => img.primary) ?? product.images[0];

  const handleAddToCart = async (e: React.MouseEvent) => {
    e.preventDefault(); // Prevent navigation to product page
    e.stopPropagation();
    try {
        await addItem(product, 1);
        addToast(t.product.added_to_cart || "Added to cart", "success");
    } catch (error) {
        // error handled in store or ignore
        console.error(error);
    }
  };

  return (
    <Link href={`/products/${product.id}`} className="block h-full group">
      <Card className="relative flex h-full flex-col overflow-hidden border-zinc-200 bg-white transition-all duration-300 hover:shadow-xl hover:-translate-y-1 hover:border-emerald-100">
      {/* Image Section */}
        <div className="relative aspect-[4/5] w-full overflow-hidden bg-zinc-50">
          {primaryImage ? (
            <Image
              src={primaryImage.url}
              alt={primaryImage.altText ?? product.name}
              fill
              className="object-cover transition-transform duration-500 group-hover:scale-105"
              sizes="(max-width: 768px) 100vw, 33vw"
            />
          ) : (
            <div className="flex h-full items-center justify-center text-zinc-300">
              <span className="text-sm font-medium">No Image</span>
            </div>
          )}
          
          {/* Badges */}
          <div className="absolute left-3 top-3 flex flex-col gap-2 z-10">
            {product.discountPercentage !== undefined && (
              <Badge className="bg-red-500 text-white border-red-500 hover:bg-red-600 shadow-sm">
                -{product.discountPercentage}%
              </Badge>
            )}
            {product.flashSaleEndAt && (
              <Badge className="bg-amber-500 text-white border-amber-500 hover:bg-amber-600 shadow-sm">
                {t.product.flash_sale}
              </Badge>
            )}
          </div>

          {/* Quick Actions Overlay */}
           <div className="absolute bottom-0 left-0 right-0 p-4 translate-y-full transition-transform duration-300 group-hover:translate-y-0 z-20 flex gap-2 justify-center bg-gradient-to-t from-black/50 to-transparent pt-12">
               <Button 
                 size="icon" 
                 variant="secondary" 
                 className="rounded-full h-10 w-10 bg-white text-zinc-900 hover:bg-emerald-600 hover:text-white shadow-lg transition-colors border-0"
                 onClick={handleAddToCart}
                 title={t.product.add_to_cart}
               >
                 <ShoppingCart size={18} />
               </Button>
           </div>
        </div>

      {/* Content Section */}
      <div className="flex flex-1 flex-col p-4">
        {/* Category */}
        {product.category && (
            <div className="mb-1 text-xs text-zinc-500 font-medium uppercase tracking-wider truncate">
                {product.category}
            </div>
        )}

        <h3 className="text-base font-medium text-zinc-900 line-clamp-2 h-12 group-hover:text-emerald-700 transition-colors mb-1">
          {product.name}
        </h3>

        {/* Rating */}
        <div className="flex items-center gap-1 mb-3">
             <Star size={14} className="fill-amber-400 text-amber-400" />
             <span className="text-sm text-zinc-700 font-medium">{product.rating?.toFixed(1) || "New"}</span>
             <span className="text-xs text-zinc-400 mx-1">•</span>
             <span className="text-xs text-zinc-500">{formatNumber(product.soldCount)} {t.product.sold}</span>
        </div>
        
        <div className="mt-auto pt-3 border-t border-zinc-100 flex items-center justify-between">
          <div className="flex flex-col">
            {product.discountPercentage ? (
                <div className="flex items-baseline gap-2">
                    <span className="text-lg font-bold text-red-600">
                        {formatCurrency(product.price, product.currency ?? "VND")}
                    </span>
                    <span className="text-xs text-zinc-400 line-through">
                        {formatCurrency(product.price / (1 - product.discountPercentage/100), product.currency ?? "VND")}
                    </span>
                </div>
            ) : (
                <span className="text-lg font-bold text-zinc-900">
                    {formatCurrency(product.price, product.currency ?? "VND")}
                </span>
            )}
          </div>
        </div>
      </div>
      </Card>
    </Link>
  );
};
