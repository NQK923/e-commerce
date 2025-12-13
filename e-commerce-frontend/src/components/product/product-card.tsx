'use client';

import Image from "next/image";
import Link from "next/link";
import React from "react";
import { Product } from "../../types/product";
import { formatCurrency, formatNumber } from "../../utils/format";
import { Badge } from "../ui/badge";
import { Card } from "../ui/card";
import { useTranslation } from "../../providers/language-provider";

type Props = {
  product: Product;
};

export const ProductCard: React.FC<Props> = ({ product }) => {
  const { t } = useTranslation();
  const primaryImage = product.images.find((img) => img.primary) ?? product.images[0];

  return (
    <Link href={`/products/${product.id}`} className="block h-full">
      <Card className="group relative flex h-full flex-col overflow-hidden border-zinc-200 bg-white transition-all hover:shadow-lg hover:-translate-y-1">
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
        </div>

      {/* Content Section */}
      <div className="flex flex-1 flex-col p-4">
        <h3 className="text-base font-medium text-zinc-900 line-clamp-1 group-hover:underline">
          {product.name}
        </h3>
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
            {formatNumber(product.soldCount)} {t.product.sold}
          </div>
        </div>
      </div>
      </Card>
    </Link>
  );
};
