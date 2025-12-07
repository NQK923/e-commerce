'use client';

import Image from "next/image";
import Link from "next/link";
import React from "react";
import { ShoppingCart, Eye } from "lucide-react";
import { Product } from "../../types/product";
import { formatCurrency } from "../../utils/format";
import { Badge } from "../ui/badge";
import { Button } from "../ui/button";
import { Card } from "../ui/card";
import { useTranslation } from "../../providers/language-provider";

type Props = {
  product: Product;
  onAddToCart?: (product: Product) => void;
};

export const ProductCard: React.FC<Props> = ({ product, onAddToCart }) => {
  const { t } = useTranslation();
  const primaryImage = product.images.find((img) => img.primary) ?? product.images[0];

  return (
    <Card className="group relative flex h-full flex-col overflow-hidden border-zinc-200 bg-white transition-all hover:shadow-lg">
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
        <div className="absolute left-3 top-3 flex flex-col gap-2">
          {product.discountPercentage !== undefined && (
            <Badge className="bg-red-500 text-white border-red-500 hover:bg-red-600">
              -{product.discountPercentage}%
            </Badge>
          )}
          {product.flashSaleEndAt && (
            <Badge className="bg-amber-500 text-white border-amber-500 hover:bg-amber-600">
              {t.product.flash_sale}
            </Badge>
          )}
        </div>

        {/* Quick Actions Overlay (Visible on Hover) */}
        <div className="absolute bottom-0 left-0 right-0 flex translate-y-full items-center justify-center gap-2 bg-white/90 p-4 backdrop-blur transition-transform duration-300 group-hover:translate-y-0">
          <Button 
            size="sm" 
            className="w-full gap-2" 
            onClick={(e) => {
              e.preventDefault();
              onAddToCart?.(product);
            }}
          >
            <ShoppingCart size={16} />
            {t.product.add_to_cart}
          </Button>
          <Link href={`/products/${product.id}`} className="w-full">
             <Button variant="secondary" size="sm" className="w-full gap-2">
              <Eye size={16} />
              {t.product.view_details}
            </Button>
          </Link>
        </div>
      </div>

      {/* Content Section */}
      <div className="flex flex-1 flex-col p-4">
        <Link href={`/products/${product.id}`} className="group/title block">
          <h3 className="text-base font-medium text-zinc-900 line-clamp-1 group-hover/title:underline">
            {product.name}
          </h3>
        </Link>
        <p className="mt-1 text-sm text-zinc-500 line-clamp-2 h-10">
          {product.shortDescription ?? product.description}
        </p>
        
        <div className="mt-4 flex items-end justify-between border-t border-zinc-100 pt-3">
          <div className="flex flex-col">
            <span className="text-xs text-zinc-400 font-medium uppercase tracking-wider">Price</span>
            <span className="text-lg font-bold text-red-600">
              {formatCurrency(product.price, product.currency ?? "VND")}
            </span>
          </div>
          <div className="text-xs text-zinc-500">
            123 {t.product.sold}
          </div>
        </div>
      </div>
    </Card>
  );
};
