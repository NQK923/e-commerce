'use client';

import Link from "next/link";
import React from "react";
import { productApi } from "@/src/api/productApi";
import { ProductCard } from "@/src/components/product/product-card";
import { Button } from "@/src/components/ui/button";
import { Card } from "@/src/components/ui/card";
import { Spinner } from "@/src/components/ui/spinner";
import { useCart } from "@/src/store/cart-store";
import { useToast } from "@/src/components/ui/toast-provider";
import { Product } from "@/src/types/product";
import { formatCurrency } from "@/src/utils/format";

export default function HomePage() {
  const { addItem } = useCart();
  const { addToast } = useToast();
  const [products, setProducts] = React.useState<Product[]>([]);
  const [loading, setLoading] = React.useState(true);

  React.useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const response = await productApi.list({ size: 8, page: 0 });
        setProducts(response.items);
      } catch (error) {
        const message = error instanceof Error ? error.message : "Failed to load products";
        addToast(message, "error");
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, [addToast]);

  const handleAdd = async (product: Product) => {
    await addItem(product, 1);
    addToast(`${product.name} added to cart`, "success");
  };

  return (
    <div className="mx-auto flex max-w-6xl flex-col gap-10 px-4 py-10">
      <section className="grid gap-6 rounded-3xl bg-gradient-to-r from-black to-zinc-900 px-8 py-10 text-white md:grid-cols-2">
        <div className="flex flex-col gap-4">
          <p className="text-sm uppercase tracking-[0.2em] text-zinc-300">New Season</p>
          <h1 className="text-4xl font-bold leading-tight md:text-5xl">Modern commerce without the friction.</h1>
          <p className="text-base text-zinc-200">
            Discover curated products, sync your cart across devices, and checkout securely with our production-ready
            storefront.
          </p>
          <div className="flex flex-wrap gap-3">
            <Link href="/products">
              <Button size="lg" variant="secondary">
                Browse products
              </Button>
            </Link>
            <Link href="/login">
              <Button size="lg" variant="ghost" className="border border-white/20 text-white hover:bg-white/10">
                Sign in
              </Button>
            </Link>
          </div>
        </div>
        <div className="flex flex-col justify-center gap-4 rounded-2xl bg-white/5 p-6 shadow-lg">
          <div className="flex items-center justify-between">
            <span className="text-sm text-zinc-300">Flash sale spotlight</span>
            <span className="rounded-full bg-white/10 px-3 py-1 text-xs font-semibold uppercase text-white">
              Limited
            </span>
          </div>
          <div className="grid grid-cols-3 gap-3">
            {products.slice(0, 3).map((product) => (
              <Card key={product.id} className="overflow-hidden border-0 bg-white/5 p-3 text-white">
                <div className="text-sm font-semibold line-clamp-2">{product.name}</div>
                <div className="text-xs text-zinc-300 line-clamp-2">{product.shortDescription ?? product.description}</div>
                <div className="mt-2 text-base font-bold">{formatCurrency(product.price, product.currency ?? "USD")}</div>
              </Card>
            ))}
            {!products.length && (
              <div className="col-span-3 text-sm text-zinc-300">Fresh arrivals will appear here.</div>
            )}
          </div>
        </div>
      </section>

      <section className="flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-bold text-black">Featured products</h2>
          <Link href="/products" className="text-sm font-semibold text-black hover:underline">
            View all
          </Link>
        </div>
        {loading ? (
          <div className="flex items-center gap-3 text-sm text-zinc-600">
            <Spinner />
            Loading products...
          </div>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {products.map((product) => (
              <ProductCard key={product.id} product={product} onAddToCart={handleAdd} />
            ))}
            {!products.length && (
              <div className="rounded-xl border border-dashed border-zinc-200 bg-white p-6 text-sm text-zinc-600">
                No products found. Try refreshing or check back later.
              </div>
            )}
          </div>
        )}
      </section>
    </div>
  );
}
