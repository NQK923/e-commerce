'use client';

import Link from "next/link";
import React from "react";
import {
    ArrowRight,
    ShieldCheck,
    Truck,
    Store,
    ChevronRight, Smartphone, Shirt, HomeIcon, Clock,
} from "lucide-react";import { productApi } from "@/src/api/productApi";
import { ProductCard } from "@/src/components/product/product-card";
import { Button } from "@/src/components/ui/button";
import { Spinner } from "@/src/components/ui/spinner";
import { useCart } from "@/src/store/cart-store";
import { useToast } from "@/src/components/ui/toast-provider";
import { Product } from "@/src/types/product";
import { useTranslation } from "@/src/providers/language-provider";

export default function HomePage() {
  const { addItem } = useCart();
  const { addToast } = useToast();
  const { t } = useTranslation();
  const [products, setProducts] = React.useState<Product[]>([]);
  const [loading, setLoading] = React.useState(true);

  React.useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const response = await productApi.list({ size: 8, page: 0 });
        setProducts(response.items);
      } catch (error) {
        const message = error instanceof Error ? error.message : t.common.error;
        addToast(message, "error");
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, [addToast, t]);

  const handleAdd = async (product: Product) => {
    await addItem(product, 1);
    addToast(`${product.name} ${t.cart.added}`, "success");
  };

  return (
    <div className="flex flex-col gap-16 pb-20">
      {/* Hero Section - Marketplace Style */}
      <section className="relative overflow-hidden bg-emerald-700 py-20 text-white sm:py-28">
        <div className="absolute inset-0 bg-[linear-gradient(to_right,#ffffff1a_1px,transparent_1px),linear-gradient(to_bottom,#ffffff1a_1px,transparent_1px)] bg-[size:2rem_2rem] opacity-20" />
        <div className="relative mx-auto flex max-w-7xl flex-col items-center gap-8 px-4 text-center sm:items-start sm:text-left">
          <div className="inline-flex items-center rounded-full bg-white/10 px-3 py-1 text-sm font-medium backdrop-blur border border-white/20 text-emerald-50">
            <span className="mr-2 inline-block h-2 w-2 animate-pulse rounded-full bg-yellow-400" />
            {t.home.hero_badge}
          </div>
          
          <h1 className="max-w-3xl text-5xl font-extrabold tracking-tight sm:text-7xl leading-[1.1]">
            {t.home.hero_title} <span className="text-yellow-300">{t.home.hero_title_highlight}</span>
          </h1>
          
          <p className="max-w-2xl text-lg text-emerald-100 sm:text-xl">
            {t.home.hero_desc}
          </p>

          <div className="mt-2 flex flex-wrap gap-4">
            <Link href="/products">
              <Button size="lg" className="h-12 gap-2 bg-white text-emerald-800 hover:bg-emerald-50 border-0 shadow-lg shadow-emerald-900/20 font-bold">
                {t.home.shop_now} <ArrowRight className="h-4 w-4" />
              </Button>
            </Link>
            <Link href="/seller/register">
              <Button size="lg" variant="outline" className="h-12 gap-2 border-2 border-emerald-200 text-white bg-transparent hover:bg-white/10 hover:text-white font-semibold">
                <Store className="h-4 w-4" />
                {t.home.start_selling}
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Features / Trust Signals */}
      <section className="mx-auto grid max-w-7xl grid-cols-1 gap-8 px-4 sm:grid-cols-3 -mt-10 relative z-10">
        {[
          { icon: Truck, title: t.home.features.shipping, desc: t.home.features.shipping_desc },
          { icon: ShieldCheck, title: t.home.features.payment, desc: t.home.features.payment_desc },
          { icon: Clock, title: t.home.features.support, desc: t.home.features.support_desc },
        ].map((feature, i) => (
          <div key={i} className="flex items-center gap-4 rounded-xl border border-zinc-100 bg-white p-6 shadow-lg shadow-zinc-200/50 transition-transform hover:-translate-y-1">
            <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-emerald-50 text-emerald-600">
              <feature.icon size={24} />
            </div>
            <div>
              <h3 className="font-bold text-zinc-900">{feature.title}</h3>
              <p className="text-sm text-zinc-500">{feature.desc}</p>
            </div>
          </div>
        ))}
      </section>

      {/* Categories */}
      <section className="mx-auto w-full max-w-7xl px-4">
        <div className="mb-8 flex items-end justify-between">
          <h2 className="text-2xl font-bold tracking-tight text-zinc-900">{t.home.categories.title}</h2>
          <Link href="/products" className="hidden text-sm font-medium text-emerald-600 hover:underline sm:block">
            {t.home.categories.view_all} &rarr;
          </Link>
        </div>
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
          {[
            { name: t.home.categories.electronics, icon: Smartphone, color: "bg-blue-50 text-blue-600", href: "electronics" },
            { name: t.home.categories.fashion, icon: Shirt, color: "bg-rose-50 text-rose-600", href: "fashion" },
            { name: t.home.categories.home, icon: HomeIcon, color: "bg-amber-50 text-amber-600", href: "home" },
            { name: t.home.categories.books, icon: Truck, color: "bg-indigo-50 text-indigo-600", href: "books" }, // Reusing Truck as placeholder for Books
          ].map((cat, i) => (
            <Link 
              key={i} 
              href={`/products?category=${cat.href}`}
              className="group flex flex-col items-center justify-center rounded-xl border border-zinc-100 bg-white py-8 transition-all hover:border-emerald-200 hover:shadow-md"
            >
              <div className={`mb-4 flex h-14 w-14 items-center justify-center rounded-full ${cat.color} transition-transform group-hover:scale-110`}>
                <cat.icon size={28} />
              </div>
              <span className="font-medium text-zinc-900">{cat.name}</span>
            </Link>
          ))}
        </div>
      </section>

      {/* Featured Products */}
      <section className="mx-auto w-full max-w-7xl px-4">
        <div className="mb-6 rounded-xl bg-gradient-to-r from-orange-500 to-red-500 p-6 text-white shadow-lg">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-2xl font-bold uppercase tracking-wide flex items-center gap-2">
                <span className="animate-pulse">⚡</span> {t.home.featured.title}
              </h2>
              <p className="mt-1 text-orange-100 text-sm opacity-90">{t.home.featured.subtitle}</p>
            </div>
            <Link href="/products">
              <Button variant="secondary" size="sm" className="gap-1 bg-white text-orange-600 hover:bg-orange-50 border-0">
                {t.home.featured.view_all} <ChevronRight size={16} />
              </Button>
            </Link>
          </div>
        </div>

        {loading ? (
          <div className="flex h-64 flex-col items-center justify-center gap-4 rounded-xl bg-zinc-50 text-zinc-500">
            <Spinner />
            <p>{t.common.loading}</p>
          </div>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {products.map((product) => (
              <ProductCard key={product.id} product={product} onAddToCart={handleAdd} />
            ))}
            {!products.length && (
              <div className="col-span-full flex h-40 items-center justify-center rounded-xl border border-dashed border-zinc-200 bg-zinc-50 text-zinc-500">
                {t.home.featured.no_products}
              </div>
            )}
          </div>
        )}
      </section>
    </div>
  );
}
