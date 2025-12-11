'use client';

import Image from "next/image";
import React, { useMemo, useState } from "react";
import { ProductImage } from "../../types/product";
import { cx } from "../../utils/cx";

type Props = {
  images: ProductImage[];
  name: string;
};

export const ProductGallery: React.FC<Props> = ({ images, name }) => {
  const orderedImages = useMemo(() => {
    if (!images?.length) return [];
    return [...images].sort((a, b) => Number(b.primary) - Number(a.primary));
  }, [images]);
  const [activeIndex, setActiveIndex] = useState(0);
  const activeImage = orderedImages[activeIndex];

  return (
    <div className="flex flex-col gap-3">
      <div className="relative aspect-square w-full overflow-hidden rounded-xl bg-zinc-100">
        {activeImage ? (
          <>
            <Image
              src={activeImage.url}
              alt={activeImage.altText ?? name}
              fill
              className="object-cover"
              sizes="(max-width: 768px) 100vw, 50vw"
            />
          </>
        ) : (
          <div className="flex h-full items-center justify-center text-sm text-zinc-500">No image</div>
        )}
      </div>
      <div className="flex gap-2 overflow-x-auto pb-1">
        {orderedImages.map((img, idx) => (
          <button
            key={img.id}
            className={cx(
              "relative h-20 w-20 flex-shrink-0 overflow-hidden rounded-lg border",
              idx === activeIndex ? "border-black" : "border-transparent",
            )}
            onClick={() => setActiveIndex(idx)}
            type="button"
          >
            <Image src={img.url} alt={img.altText ?? name} fill className="object-cover" sizes="80px" />
          </button>
        ))}
      </div>
    </div>
  );
};
