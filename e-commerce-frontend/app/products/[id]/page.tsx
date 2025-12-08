'use client';

import { useParams } from "next/navigation";
import React, { useEffect, useMemo, useState } from "react";
import { productApi } from "@/src/api/productApi";
import { ProductGallery } from "@/src/components/product/product-gallery";
import { ReportProductDialog } from "@/src/components/product/report-product-dialog";
import { ProductReviews } from "@/src/components/product/product-reviews";
import { Button } from "@/src/components/ui/button";
import { Badge } from "@/src/components/ui/badge";
import { Spinner } from "@/src/components/ui/spinner";
import { useCart } from "@/src/store/cart-store";
import { useToast } from "@/src/components/ui/toast-provider";
import { Product } from "@/src/types/product";
import { formatCurrency } from "@/src/utils/format";
import { useTranslation } from "@/src/providers/language-provider";

type CountdownProps = { endAt: string };
const FlashSaleCountdown: React.FC<CountdownProps> = ({ endAt }) => {
  const { t } = useTranslation();
  const [remaining, setRemaining] = useState(0);

  useEffect(() => {
    const update = () => setRemaining(new Date(endAt).getTime() - Date.now());
    update();
    const timer = setInterval(update, 1000);
    return () => clearInterval(timer);
  }, [endAt]);

  if (remaining <= 0) return <span className="text-sm text-rose-600">{t.product.sale_ended}</span>;

  const hours = Math.floor(remaining / (1000 * 60 * 60));
  const minutes = Math.floor((remaining / (1000 * 60)) % 60);
  const seconds = Math.floor((remaining / 1000) % 60);

  return (
    <div className="text-sm font-semibold text-amber-700">
      {t.product.ends_in} {hours.toString().padStart(2, "0")}:{minutes.toString().padStart(2, "0")}:
      {seconds.toString().padStart(2, "0")}
    </div>
  );
};

export default function ProductDetailPage() {
  const params = useParams<{ id: string }>();
  const productId = params?.id;
  const { addItem } = useCart();
  const { addToast } = useToast();
  const { t } = useTranslation();
  const [product, setProduct] = useState<Product | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!productId) return;
    const load = async () => {
      setLoading(true);
      try {
        const detail = await productApi.detail(productId);
        setProduct(detail);
        setError(null);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Failed to load product";
        setError(message);
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, [productId]);

  const saleEnded = useMemo(() => {
    if (!product?.flashSaleEndAt) return false;
    return new Date(product.flashSaleEndAt).getTime() <= Date.now();
  }, [product?.flashSaleEndAt]);

  const outOfStock = useMemo(
    () => (product?.stock !== undefined ? product.stock <= 0 : false),
    [product?.stock],
  );

  const handleAdd = async () => {
    if (!product) return;
    if (outOfStock) {
      addToast(t.common.out_of_stock ?? "Out of stock", "error");
      return;
    }
    const available = product.stock ?? quantity;
    const clamped = Math.min(Math.max(quantity, 1), available);
    await addItem(product, clamped);
    addToast(`${product.name} ${t.product.added_to_cart}`, "success");
  };

  if (loading) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        {t.product.loading}
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="mx-auto flex max-w-3xl flex-col items-center gap-3 px-4 py-10 text-center">
        <p className="text-lg font-semibold text-zinc-900">{t.product.load_failed}</p>
        <p className="text-sm text-zinc-600">{error}</p>
        <Button variant="secondary" onClick={() => window.location.reload()}>
          {t.common.retry}
        </Button>
      </div>
    );
  }

  return (
    <div className="mx-auto grid max-w-6xl gap-10 px-4 py-10 lg:grid-cols-2">
      <ProductGallery images={product.images} name={product.name} />
      <div className="flex flex-col gap-4">
        <div className="flex items-start justify-between gap-3">
          <div>
            <h1 className="text-3xl font-bold text-zinc-900">{product.name}</h1>
            <p className="mt-2 text-sm text-zinc-600">{product.shortDescription ?? product.description}</p>
            {product.sku && <p className="text-xs text-zinc-500">{t.common.sku}: {product.sku}</p>}
          </div>
          {product.flashSaleEndAt && (
            <div className="flex flex-col items-end gap-2 rounded-lg bg-amber-50 px-3 py-2 text-right">
              <Badge tone="warning">{t.product.flash_sale}</Badge>
              <FlashSaleCountdown endAt={product.flashSaleEndAt} />
            </div>
          )}
        </div>

        <div className="flex items-center gap-3 text-2xl font-bold text-red-600">
          {formatCurrency(product.price, product.currency ?? "USD")}
          {product.discountPercentage !== undefined && (
            <Badge tone="success">-{product.discountPercentage}%</Badge>
          )}
        </div>

        <div className="flex flex-wrap gap-3 text-sm text-zinc-700">
          <span>
            {t.product.availability}:{" "}
            {product.stock !== undefined ? (product.stock > 0 ? t.common.in_stock : t.common.out_of_stock) : t.common.in_stock}
          </span>
          {product.rating !== undefined && <span>{t.product.rating}: {product.rating.toFixed(1)} / 5</span>}
          {product.category && <span className="rounded-full bg-zinc-100 px-2 py-1">#{product.category}</span>}
        </div>

        <div className="flex items-center gap-4">
          <input
            type="number"
            min={1}
            max={product?.stock ?? undefined}
            value={quantity}
            onChange={(e) => {
              const next = Math.max(1, Number(e.target.value));
              if (product?.stock !== undefined) {
                setQuantity(Math.min(next, product.stock));
              } else {
                setQuantity(next);
              }
            }}
            className="w-24 rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:border-emerald-600 focus:outline-none focus:ring-2 focus:ring-emerald-600/10"
          />
          <Button onClick={handleAdd} disabled={outOfStock || saleEnded} className="bg-emerald-600 hover:bg-emerald-700">
            {saleEnded ? t.product.sale_ended : t.product.add_to_cart}
          </Button>
        </div>

        {saleEnded && (
          <div className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">
            Flash sale has ended for this product.
          </div>
        )}

        <div className="mt-4 rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm">
          <h3 className="text-lg font-semibold text-zinc-900">{t.product.description}</h3>
          <p className="mt-2 text-sm leading-relaxed text-zinc-700">{product.description}</p>
        </div>

        <div className="mt-2">
             <ProductReviews productId={product.id} />
        </div>

        <div className="mt-2">
             <ReportProductDialog productId={product.id} productName={product.name} />
        </div>
      </div>
    </div>
  );
}
