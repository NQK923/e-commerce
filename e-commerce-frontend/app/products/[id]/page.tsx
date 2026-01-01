'use client';

import { useParams } from "next/navigation";
import React, { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { 
  Share2, ShieldCheck, Truck, RefreshCcw, 
  Minus, Plus, ChevronRight, Home, MessageSquare, Flag
} from "lucide-react";

import { productApi } from "@/src/api/productApi";
import { userApi } from "@/src/api/userApi";
import { ProductGallery } from "@/src/components/product/product-gallery";
import { ReportProductDialog } from "@/src/components/product/report-product-dialog";
import { ProductReviews } from "@/src/components/product/product-reviews";
import { Button } from "@/src/components/ui/button";
import { Badge } from "@/src/components/ui/badge";
import { Spinner } from "@/src/components/ui/spinner";
import { openChatWidget } from "@/src/lib/chat-widget-controller";
import { useCart } from "@/src/store/cart-store";
import { useAuth } from "@/src/store/auth-store";
import { useToast } from "@/src/components/ui/toast-provider";
import { Product } from "@/src/types/product";
import { User } from "@/src/types/auth";
import { formatCurrency } from "@/src/utils/format";
import { useTranslation } from "@/src/providers/language-provider";
import { Store } from "lucide-react";

// --- Components ---

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

  if (remaining <= 0) return <span className="text-sm font-medium text-rose-600">{t.product.sale_ended}</span>;

  const hours = Math.floor(remaining / (1000 * 60 * 60));
  const minutes = Math.floor((remaining / (1000 * 60)) % 60);
  const seconds = Math.floor((remaining / 1000) % 60);

  return (
    <div className="flex items-center gap-1 text-sm font-bold text-amber-600">
      <span>{t.product.ends_in}</span>
      <span className="rounded bg-amber-100 px-1">{hours.toString().padStart(2, "0")}</span>:
      <span className="rounded bg-amber-100 px-1">{minutes.toString().padStart(2, "0")}</span>:
      <span className="rounded bg-amber-100 px-1">{seconds.toString().padStart(2, "0")}</span>
    </div>
  );
};

const ProductCardSimple = ({ product }: { product: Product }) => {
  return (
    <Link href={`/products/${product.id}`} className="group block">
      <div className="overflow-hidden rounded-xl border border-zinc-200 bg-white transition hover:shadow-md">
        <div className="relative aspect-square overflow-hidden bg-zinc-100">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img 
            src={product.images[0]?.url || "https://placehold.co/400"} 
            alt={product.name}
            className="h-full w-full object-cover transition duration-300 group-hover:scale-105"
          />
        </div>
        <div className="p-3">
          <h3 className="line-clamp-2 text-sm font-medium text-zinc-900 group-hover:text-emerald-600">
            {product.name}
          </h3>
          <div className="mt-2 font-bold text-red-600">
            {formatCurrency(product.price, product.currency ?? "VND")}
          </div>
        </div>
      </div>
    </Link>
  );
};

// --- Main Page Component ---

export default function ProductDetailPage() {
  const params = useParams<{ id: string }>();
  const productId = params?.id;
  const { addItem } = useCart();
  const { user } = useAuth();
  const { addToast } = useToast();
  const { t } = useTranslation();
  
  const [product, setProduct] = useState<Product | null>(null);
  const [seller, setSeller] = useState<User | null>(null);
  const [relatedProducts, setRelatedProducts] = useState<Product[]>([]);
  const [selectedVariantIndex, setSelectedVariantIndex] = useState<number | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Load Main Product
  useEffect(() => {
    if (!productId) return;
    const load = async () => {
      setLoading(true);
      try {
        const detail = await productApi.detail(productId);
        setProduct(detail);
        if (detail.variants && detail.variants.length > 0) {
            setSelectedVariantIndex(0);
        }
        
        if (detail.sellerId) {
            userApi.getById(detail.sellerId).then(setSeller).catch(console.error);
        }

        setError(null);
        
        // Fetch Related Products (Same Category)
        if (detail.category) {
            try {
                const related = await productApi.list({ category: detail.category, size: 4 });
                setRelatedProducts(related.items.filter(p => p.id !== detail.id));
            } catch (e) {
                console.error("Failed to load related products", e);
            }
        }

      } catch (err) {
        const message = err instanceof Error ? err.message : "Failed to load product";
        setError(message);
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, [productId]);

  // Derived State for Display (Merging Base + Variant)
  const displayProduct = useMemo(() => {
      if (!product) return null;
      if (selectedVariantIndex !== null && product.variants && product.variants[selectedVariantIndex]) {
          const v = product.variants[selectedVariantIndex];
          return {
              ...product,
              price: v.price,
              stock: v.quantity,
              sku: v.sku,
          };
      }
      return product;
  }, [product, selectedVariantIndex]);

  const saleEnded = useMemo(() => {
    if (!displayProduct?.flashSaleEndAt) return false;
    return new Date(displayProduct.flashSaleEndAt).getTime() <= Date.now();
  }, [displayProduct?.flashSaleEndAt]);

  const outOfStock = useMemo(
    () => (displayProduct?.stock !== undefined ? displayProduct.stock <= 0 : false),
    [displayProduct?.stock],
  );

  const lowStock = useMemo(
    () => (displayProduct?.stock !== undefined ? displayProduct.stock > 0 && displayProduct.stock <= 5 : false),
    [displayProduct?.stock],
  );

  const handleAdd = async () => {
    if (!displayProduct) return;
    if (outOfStock) {
      addToast(t.common.out_of_stock ?? "Out of stock", "error");
      return;
    }
    const available = displayProduct.stock ?? quantity;
    const clamped = Math.min(Math.max(quantity, 1), available);
    
    const variantSku = selectedVariantIndex !== null && product?.variants && product.variants[selectedVariantIndex]
        ? product.variants[selectedVariantIndex].sku
        : undefined;

    await addItem(displayProduct, clamped, variantSku);
    addToast(`${displayProduct.name} ${t.product.added_to_cart}`, "success");
  };

  const adjustQuantity = (delta: number) => {
      if (!displayProduct) return;
      const max = displayProduct.stock ?? 999;
      setQuantity(prev => Math.min(Math.max(1, prev + delta), max));
  };

  if (loading) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        {t.product.loading}
      </div>
    );
  }

  if (error || !displayProduct || !product) {
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
    <div className="bg-zinc-50/50 min-h-screen pb-20">
      {/* Breadcrumbs */}
      <div className="border-b border-zinc-200 bg-white">
        <div className="mx-auto flex max-w-7xl items-center gap-2 px-4 py-3 text-sm text-zinc-500">
            <Link href="/" className="flex items-center hover:text-emerald-600">
                <Home size={14} className="mr-1"/> {t.nav.home}
            </Link>
            <ChevronRight size={14} />
            <Link href={`/search?category=${product.category}`} className="hover:text-emerald-600">
                {product.category || t.nav.products}
            </Link>
            <ChevronRight size={14} />
            <span className="truncate font-medium text-zinc-900 max-w-[200px]">{product.name}</span>
        </div>
      </div>

      <div className="mx-auto max-w-7xl px-4 py-8">
        <div className="grid grid-cols-1 gap-8 lg:grid-cols-12">
            
          {/* Left Column: Gallery */}
          <div className="lg:col-span-5">
            <ProductGallery images={displayProduct.images} name={displayProduct.name} />
          </div>

          {/* Right Column: Info & Actions */}
          <div className="lg:col-span-7">
             <div className="sticky top-24 space-y-6">
                {/* Title & Price Card */}
                <div className="rounded-2xl bg-white p-6 shadow-sm border border-zinc-200">
                    <div className="flex items-start justify-between gap-4">
                        <div>
                            <h1 className="text-2xl font-bold text-zinc-900 sm:text-3xl">{displayProduct.name}</h1>
                            <div className="mt-2 flex items-center gap-4 text-sm">
                                <div className="flex items-center gap-1 text-amber-500">
                                    <span>★</span>
                                    <span className="font-bold text-zinc-900">{displayProduct.rating?.toFixed(1) || "5.0"}</span>
                                </div>
                                {displayProduct.soldCount !== undefined && displayProduct.soldCount >= 0 && (
                                    <>
                                        <span className="text-zinc-300">|</span>
                                        <span className="text-zinc-500">{t.product.sold} {displayProduct.soldCount}</span>
                                    </>
                                )}
                                {displayProduct.sku && (
                                    <>
                                        <span className="text-zinc-300">|</span>
                                        <span className="text-zinc-500">{t.common.sku}: {displayProduct.sku}</span>
                                    </>
                                )}
                            </div>
                        </div>
                        <ReportProductDialog productId={displayProduct.id} productName={displayProduct.name}>
                            <Button variant="ghost" size="icon" className="text-zinc-400 hover:text-red-500" title={t.common.report}>
                                <Flag size={20} />
                            </Button>
                        </ReportProductDialog>
                    </div>

                    <div className="mt-6 rounded-xl bg-zinc-50 p-4">
                        <div className="flex items-end gap-3">
                            <span className="text-3xl font-bold text-red-600">
                                {formatCurrency(displayProduct.price, displayProduct.currency ?? "VND")}
                            </span>
                            {displayProduct.discountPercentage && displayProduct.discountPercentage > 0 && (
                                <>
                                    <span className="mb-1 text-sm text-zinc-400 line-through">
                                        {formatCurrency(displayProduct.price * (1 + displayProduct.discountPercentage/100), displayProduct.currency ?? "VND")}
                                    </span>
                                    <Badge tone="danger" className="mb-1">-{displayProduct.discountPercentage}%</Badge>
                                </>
                            )}
                        </div>
                        {displayProduct.flashSaleEndAt && (
                            <div className="mt-3 flex items-center gap-2 border-t border-zinc-200 pt-3">
                                <Badge tone="warning">FLASH SALE</Badge>
                                <FlashSaleCountdown endAt={displayProduct.flashSaleEndAt} />
                            </div>
                        )}
                        {lowStock && (
                            <div className="mt-3 inline-flex items-center gap-2 rounded-lg bg-red-50 px-3 py-2 text-sm font-semibold text-red-700 border border-red-100">
                                {t.product.only_left.replace("{{count}}", (displayProduct.stock || 0).toString())}
                            </div>
                        )}
                    </div>

                    {/* Variant Selection */}
                    {product.variants && product.variants.length > 0 && (
                        <div className="mt-6 space-y-3">
                            <span className="text-sm font-medium text-zinc-900">{t.product.variant_select}</span>
                            <div className="flex flex-wrap gap-2">
                                {product.variants.map((v, idx) => (
                                    <button
                                        key={idx}
                                        onClick={() => setSelectedVariantIndex(idx)}
                                        className={`min-w-[4rem] rounded-lg border px-4 py-2 text-sm transition-all
                                            ${selectedVariantIndex === idx 
                                                ? 'border-emerald-600 bg-emerald-50 font-bold text-emerald-700 ring-1 ring-emerald-600' 
                                                : 'border-zinc-200 bg-white text-zinc-700 hover:border-zinc-300'
                                            }
                                        `}
                                    >
                                        {v.name}
                                    </button>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Quantity & Actions */}
                    <div className="mt-8 flex flex-col gap-4 sm:flex-row sm:items-end">
                        <div className="space-y-3">
                             <span className="text-sm font-medium text-zinc-900">{t.product.quantity_select}</span>
                             <div className="flex items-center rounded-lg border border-zinc-200 bg-white w-fit">
                                <button 
                                    onClick={() => adjustQuantity(-1)}
                                    disabled={quantity <= 1}
                                    className="p-3 text-zinc-500 hover:bg-zinc-50 hover:text-zinc-900 disabled:opacity-30"
                                >
                                    <Minus size={16} />
                                </button>
                                <input
                                    type="number"
                                    value={quantity}
                                    readOnly
                                    className="w-12 border-none bg-transparent p-0 text-center text-sm font-bold text-zinc-900 focus:ring-0"
                                />
                                <button 
                                    onClick={() => adjustQuantity(1)}
                                    disabled={!displayProduct.stock || quantity >= displayProduct.stock}
                                    className="p-3 text-zinc-500 hover:bg-zinc-50 hover:text-zinc-900 disabled:opacity-30"
                                >
                                    <Plus size={16} />
                                </button>
                             </div>
                             <p className="text-xs text-zinc-500">
                                {displayProduct.stock !== undefined
                                  ? lowStock
                                    ? t.product.only_left.replace("{{count}}", displayProduct.stock.toString())
                                    : t.product.left_in_stock.replace("{{count}}", displayProduct.stock.toString())
                                  : t.common.out_of_stock}
                             </p>
                        </div>

                        <div className="flex flex-1 gap-3">
                            <Button 
                                onClick={handleAdd} 
                                disabled={outOfStock || saleEnded} 
                                className="h-12 flex-1 bg-emerald-600 text-base font-semibold hover:bg-emerald-700 shadow-emerald-200 shadow-lg"
                            >
                                {saleEnded ? t.product.sale_ended : outOfStock ? t.common.out_of_stock : t.product.add_to_cart}
                            </Button>
                            
                            <Button variant="outline" className="h-12 w-12 border-zinc-200 text-zinc-500 hover:bg-zinc-50 hover:text-emerald-600">
                                <Share2 size={20} />
                            </Button>
                        </div>
                    </div>

                    {/* Shop Info Card */}
                    <div className="mt-8 border-t border-zinc-200 pt-6">
                        {seller ? (
                            <div className="flex items-center gap-4">
                                <div className="h-14 w-14 overflow-hidden rounded-full border border-zinc-200 bg-zinc-100">
                                    {/* eslint-disable-next-line @next/next/no-img-element */}
                                    <img 
                                        src={seller.avatarUrl || "https://placehold.co/100?text=Shop"} 
                                        alt={seller.displayName}
                                        className="h-full w-full object-cover"
                                    />
                                </div>
                                <div className="flex-1">
                                    <h4 className="font-bold text-zinc-900">{seller.displayName}</h4>
                                    <div className="flex items-center gap-2 text-xs text-zinc-500">
                                        <span className="flex items-center gap-1"><Store size={12}/> {t.common.online}</span>
                                        <span>•</span>
                                        <span>{t.product.rating} 4.9 (1k+)</span>
                                    </div>
                                </div>
                                <div className="flex flex-col gap-2 sm:flex-row">
                                    {displayProduct.sellerId && displayProduct.sellerId !== user?.id && (
                                      <Button
                                        type="button"
                                        variant="outline"
                                        size="sm"
                                        className="w-full text-emerald-600 hover:bg-emerald-50 border-emerald-200"
                                        onClick={() => openChatWidget({ userId: displayProduct.sellerId })}
                                      >
                                        <MessageSquare size={16} className="mr-2" /> {t.product.chat}
                                      </Button>
                                    )}
                                    <Link href={`/shop/${seller.id}`}>
                                        <Button variant="secondary" size="sm" className="w-full">
                                            {t.product.view_shop}
                                        </Button>
                                    </Link>
                                </div>
                            </div>
                        ) : (
                            /* Fallback for System/Legacy Products or Loading */
                            <div className="flex items-center gap-4">
                                <div className="h-14 w-14 overflow-hidden rounded-full border border-zinc-200 bg-zinc-100 flex items-center justify-center">
                                    <Store size={24} className="text-zinc-400" />
                                </div>
                                <div className="flex-1">
                                    <h4 className="font-bold text-zinc-900">{t.product.official_store}</h4>
                                    <div className="flex items-center gap-2 text-xs text-zinc-500">
                                        <span className="flex items-center gap-1"><ShieldCheck size={12} className="text-emerald-500"/> {t.product.verified}</span>
                                    </div>
                                </div>
                                <div className="flex flex-col gap-2 sm:flex-row">
                                   <Button variant="secondary" size="sm" disabled>
                                        {t.product.view_shop}
                                    </Button>
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                {/* Policy / Trust Badges */}
                <div className="grid grid-cols-3 gap-4 rounded-xl border border-zinc-200 bg-white p-4 text-center shadow-sm">
                    <div className="flex flex-col items-center gap-2">
                        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-emerald-50 text-emerald-600">
                            <ShieldCheck size={20} />
                        </div>
                        <span className="text-xs font-medium text-zinc-600">{t.product.genuine_100}</span>
                    </div>
                    <div className="flex flex-col items-center gap-2 border-l border-zinc-100">
                        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-emerald-50 text-emerald-600">
                            <Truck size={20} />
                        </div>
                        <span className="text-xs font-medium text-zinc-600">{t.product.free_shipping}</span>
                    </div>
                    <div className="flex flex-col items-center gap-2 border-l border-zinc-100">
                        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-emerald-50 text-emerald-600">
                            <RefreshCcw size={20} />
                        </div>
                        <span className="text-xs font-medium text-zinc-600">{t.product.return_7_days}</span>
                    </div>
                </div>
             </div>
          </div>
        </div>

        {/* Bottom Section: Full Description & Reviews */}
        <div className="mt-10 grid gap-8 lg:grid-cols-12">
            <div className="lg:col-span-12 space-y-8">
                 <div className="rounded-2xl border border-zinc-200 bg-white p-8 shadow-sm">
                    <h3 className="mb-6 text-xl font-bold text-zinc-900 border-b border-zinc-100 pb-4">{t.product.description}</h3>
                    <div className="prose prose-sm max-w-none text-zinc-700 leading-normal">
                        <p className="whitespace-pre-line">{displayProduct.description}</p>
                    </div>
                 </div>

                 <div className="rounded-2xl border border-zinc-200 bg-white p-8 shadow-sm">
                    <ProductReviews productId={displayProduct.id} sellerId={displayProduct.sellerId} />
                 </div>
                 
            </div>
        </div>

        {/* Related Products */}
        {relatedProducts.length > 0 && (
            <div className="mt-12 border-t border-zinc-200 pt-10">
                <h2 className="mb-6 text-2xl font-bold text-zinc-900">{t.product.similar_products}</h2>
                <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-4">
                    {relatedProducts.map(p => (
                        <ProductCardSimple key={p.id} product={p} />
                    ))}
                </div>
            </div>
        )}

      </div>
    </div>
  );
}
