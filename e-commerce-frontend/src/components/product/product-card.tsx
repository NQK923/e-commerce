'use client';

import Image from "next/image";
import Link from "next/link";
import React from "react";
import { Product } from "../../types/product";
import { formatCurrency } from "../../utils/format";
import { Badge } from "../ui/badge";
import { Button } from "../ui/button";
import { Card } from "../ui/card";

type Props = {
  product: Product;
  onAddToCart?: (product: Product) => void;
};

export const ProductCard: React.FC<Props> = ({ product, onAddToCart }) => {
  const primaryImage = product.images.find((img) => img.primary) ?? product.images[0];

  return (
    <Card className="flex h-full flex-col overflow-hidden">
      <div className="relative h-48 w-full overflow-hidden bg-zinc-100">
        {primaryImage ? (
          <Image
            src={primaryImage.url}
            alt={primaryImage.altText ?? product.name}
            fill
            className="object-cover"
            sizes="(max-width: 768px) 100vw, 33vw"
          />
        ) : (
          <div className="flex h-full items-center justify-center text-sm text-zinc-500">No image</div>
        )}
        {product.discountPercentage !== undefined && (
          <div className="absolute left-3 top-3">
            <Badge tone="success">-{product.discountPercentage}%</Badge>
          </div>
        )}
        {product.flashSaleEndAt && (
          <div className="absolute right-3 top-3">
            <Badge tone="warning">Flash sale</Badge>
          </div>
        )}
      </div>
      <div className="flex flex-1 flex-col gap-3 p-4">
        <div className="flex flex-col gap-1">
          <Link href={`/products/${product.id}`} className="text-lg font-semibold text-black hover:underline">
            {product.name}
          </Link>
          <p className="text-sm text-zinc-600 line-clamp-2">{product.shortDescription ?? product.description}</p>
        </div>
        <div className="mt-auto flex items-center justify-between">
          <div className="text-lg font-bold text-black">
            {formatCurrency(product.price, product.currency ?? "USD")}
          </div>
          <div className="flex gap-2">
            <Link href={`/products/${product.id}`}>
              <Button variant="secondary" size="sm">
                Details
              </Button>
            </Link>
            <Button size="sm" onClick={() => onAddToCart?.(product)}>
              Add to cart
            </Button>
          </div>
        </div>
      </div>
    </Card>
  );
};
