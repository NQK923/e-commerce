'use client';

import Image from "next/image";
import Link from "next/link";
import React from "react";
import { Product } from "../../types/product";
import { formatCurrency, formatNumber } from "../../utils/format";
import { Badge } from "../ui/badge";
import { useTranslation } from "../../providers/language-provider";

type Props = {
  product: Product;
};

export const ProductCard: React.FC<Props> = ({ product }) => {
  const { t } = useTranslation();
  const primaryImage = product.images.find((img) => img.primary) ?? product.images[0];
  const secondaryImage = product.images.find((img) => !img.primary) ?? primaryImage;

  return (
    <Link href={`/products/${product.id}`} className="block h-full group">
      <div className="relative flex h-full flex-col bg-transparent">
        {/* Image Section */}
        <div className="relative aspect-[3/4] w-full overflow-hidden bg-zinc-100 mb-4">
          {primaryImage ? (
            <>
              <Image
                src={primaryImage.url}
                alt={primaryImage.altText ?? product.name}
                fill
                className={`object-cover transition-opacity duration-500 ease-in-out ${secondaryImage && secondaryImage.url !== primaryImage.url ? 'group-hover:opacity-0' : 'group-hover:scale-105'}`}
                sizes="(max-width: 768px) 100vw, 33vw"
              />
              {secondaryImage && secondaryImage.url !== primaryImage.url && (
                <Image
                  src={secondaryImage.url}
                  alt={secondaryImage.altText ?? product.name}
                  fill
                  className="object-cover transition-opacity duration-500 ease-in-out opacity-0 group-hover:opacity-100"
                  sizes="(max-width: 768px) 100vw, 33vw"
                />
              )}
            </>
          ) : (
            <div className="flex h-full items-center justify-center text-zinc-300">
              <span className="text-sm font-medium">{t.common.no_image}</span>
            </div>
          )}
          
          {/* Badges */}
          <div className="absolute left-0 top-0 flex flex-col gap-2 z-10 p-3">
            {product.discountPercentage !== undefined && product.discountPercentage > 0 && (
              <Badge className="rounded-md bg-red-600 text-white hover:bg-red-700 font-bold px-2 py-0.5 text-xs">
                -{product.discountPercentage}%
              </Badge>
            )}
            {product.flashSaleEndAt && (
              <Badge className="rounded-md bg-black text-white hover:bg-black px-2 py-0.5 text-xs uppercase tracking-wider">
                {t.product.flash_sale}
              </Badge>
            )}
          </div>
        </div>

        {/* Content Section */}
        <div className="flex flex-1 flex-col">
          {/* Category & Rating Row */}
          <div className="flex items-center justify-between mb-2">
            {product.category ? (
                <div className="text-[10px] text-zinc-500 font-medium uppercase tracking-widest truncate">
                    {product.category}
                </div>
            ) : (
                <div />
            )}
            
            {/* Rating */}
            <div className="flex items-center gap-1 text-[11px] font-medium text-zinc-500">
                <span>★ {product.rating?.toFixed(1) || t.product.new}</span>
                <span>({formatNumber(product.soldCount)})</span>
            </div>
          </div>

          <h3 className="text-sm font-medium text-black line-clamp-1 group-hover:underline decoration-1 underline-offset-4 mb-2 transition-all">
            {product.name}
          </h3>
          
          <div className="mt-auto flex items-center gap-2">
            {product.discountPercentage && product.discountPercentage > 0 ? (
                <>
                    <span className="text-sm font-bold text-red-600">
                        {formatCurrency(product.price, product.currency ?? "VND")}
                    </span>
                    <span className="text-xs text-zinc-400 line-through">
                        {formatCurrency(product.price / (1 - product.discountPercentage/100), product.currency ?? "VND")}
                    </span>
                </>
            ) : (
                <span className="text-sm font-bold text-black">
                    {formatCurrency(product.price, product.currency ?? "VND")}
                </span>
            )}
          </div>
        </div>
      </div>
    </Link>
  );
};
