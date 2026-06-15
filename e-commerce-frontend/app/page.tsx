'use client';

import Link from "next/link";
import Image from "next/image";
import React, { useCallback, useEffect, useState } from "react";
import {
  ArrowRight,
  ShieldCheck,
  Truck,
  Clock,
  Sparkles,
} from "lucide-react";
import { productApi } from "@/src/api/productApi";
import { ProductCard } from "@/src/components/product/product-card";
import { Button } from "@/src/components/ui/button";
import { Skeleton } from "@/src/components/ui/skeleton";
import { useTranslation } from "@/src/providers/language-provider";
import { useToast } from "@/src/components/ui/toast-provider";
import { Product } from "@/src/types/product";

export default function HomePage() {
  const { t } = useTranslation();
  const { addToast } = useToast();

  const [bestSellers, setBestSellers] = useState<Product[]>([]);
  const [newArrivals, setNewArrivals] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    setLoading(true);
    setLoadError(null);
    try {
      const [bestSellerData, newArrivalData] = await Promise.all([
        productApi.list({ size: 4, sort: "soldCount,desc", page: 0 }),
        productApi.list({ size: 8, sort: "createdAt,desc", page: 0 }),
      ]);
      setBestSellers(bestSellerData.items);
      setNewArrivals(newArrivalData.items);
    } catch (error) {
      console.error(error);
      const message = t.product.load_failed;
      setLoadError(message);
      addToast(message, "error");
    } finally {
      setLoading(false);
    }
  }, [addToast, t]);

  useEffect(() => {
    void loadData();
  }, [loadData]);

  const Benefits = [
    { icon: Truck, title: t.home.features.shipping, desc: t.home.features.shipping_desc },
    { icon: ShieldCheck, title: t.home.features.payment, desc: t.home.features.payment_desc },
    { icon: Clock, title: t.home.features.support, desc: t.home.features.support_desc },
  ];

  const renderErrorState = () => (
    <div className="col-span-full border border-black p-6 text-center">
      <p className="text-sm font-medium text-black">{loadError}</p>
      <Button variant="outline" size="sm" className="mt-4 rounded-md border-black hover:bg-black hover:text-white" onClick={() => void loadData()}>
        {t.common.retry}
      </Button>
    </div>
  );

  return (
    <div className="flex flex-col bg-white">
      
      {/* 1. Minimalist Hero Section */}
      <section className="relative h-[80vh] w-full bg-zinc-100 flex items-center overflow-hidden">
        <Image
          src="/images/hero-bg-v3.png"
          alt="Hero background"
          fill
          priority
          sizes="100vw"
          className="object-cover opacity-90"
        />
        <div className="absolute inset-0 bg-black/20" /> {/* Subtle overlay */}
        
        <div className="relative mx-auto w-full max-w-7xl px-6 lg:px-8 z-10 flex flex-col justify-center">
          <div className="max-w-2xl text-white">
            <h1 className="text-5xl font-light tracking-tight sm:text-7xl mb-6">
              Essential<br/><span className="font-bold">Elegance.</span>
            </h1>
            <p className="text-lg text-white/90 mb-10 max-w-md font-light">
              Khám phá bộ sưu tập sản phẩm tuyển chọn mang đậm phong cách tối giản và hiện đại.
            </p>
            <div className="flex gap-4">
              <Link href="/products">
                <Button size="lg" className="rounded-md h-14 px-10 bg-white text-black hover:bg-zinc-200 text-sm tracking-widest font-semibold uppercase">
                  {t.home.shop_now}
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* 2. Editorial Categories */}
      <section className="mx-auto w-full max-w-7xl px-6 lg:px-8 py-24">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <Link href="/products?category=fashion" className="group relative h-[500px] overflow-hidden bg-zinc-100">
            <Image
              src="/images/cat-fashion-v2.png"
              alt="Fashion"
              fill
              className="object-cover transition-transform duration-700 group-hover:scale-105"
            />
            <div className="absolute inset-0 bg-black/10 transition-opacity group-hover:bg-black/20" />
            <div className="absolute bottom-10 left-10 text-white">
              <h2 className="text-3xl font-bold tracking-tight mb-2">Fashion</h2>
              <span className="text-sm uppercase tracking-widest flex items-center gap-2">
                Explore <ArrowRight size={16} className="transition-transform group-hover:translate-x-2" />
              </span>
            </div>
          </Link>
          
          <div className="grid grid-rows-2 gap-8">
            <Link href="/products?category=electronics" className="group relative h-full overflow-hidden bg-zinc-100">
              <Image
                src="/images/cat-electronics-v3.png"
                alt="Electronics"
                fill
                className="object-cover transition-transform duration-700 group-hover:scale-105"
              />
              <div className="absolute inset-0 bg-black/10 transition-opacity group-hover:bg-black/20" />
              <div className="absolute bottom-8 left-8 text-white">
                <h2 className="text-2xl font-bold tracking-tight mb-2">Electronics</h2>
              </div>
            </Link>
            <Link href="/products?category=home" className="group relative h-full overflow-hidden bg-zinc-100">
              <Image
                src="/images/cat-home-v2.png"
                alt="Home & Living"
                fill
                className="object-cover transition-transform duration-700 group-hover:scale-105"
              />
              <div className="absolute inset-0 bg-black/10 transition-opacity group-hover:bg-black/20" />
              <div className="absolute bottom-8 left-8 text-white">
                <h2 className="text-2xl font-bold tracking-tight mb-2">Living</h2>
              </div>
            </Link>
          </div>
        </div>
      </section>

      {/* 3. Curated Best Sellers */}
      <section className="mx-auto w-full max-w-7xl px-6 lg:px-8 pb-24">
        <div className="flex items-end justify-between mb-12 border-b border-black pb-4">
           <h2 className="text-2xl font-bold uppercase tracking-wide text-black">{t.home.best_sellers_title}</h2>
           <Link href="/products?sort=soldCount,desc" className="text-sm font-medium text-zinc-500 hover:text-black uppercase tracking-widest transition-colors">
              {t.home.view_all_arrow}
           </Link>
        </div>

        {loading ? (
            <div className="grid grid-cols-1 gap-10 sm:grid-cols-2 lg:grid-cols-4">
                {[...Array(4)].map((_, i) => (
                    <div key={i} className="flex flex-col space-y-4">
                        <Skeleton className="h-[400px] w-full rounded-md" />
                        <div className="space-y-2">
                            <Skeleton className="h-4 w-3/4 rounded-md" />
                            <Skeleton className="h-4 w-1/4 rounded-md" />
                        </div>
                    </div>
                ))}
            </div>
        ) : (
            <div className="grid grid-cols-1 gap-10 sm:grid-cols-2 lg:grid-cols-4">
            {loadError ? (
              renderErrorState()
            ) : (
              <>
                {bestSellers.map((product) => (
                  <ProductCard key={product.id} product={product} />
                ))}
                {bestSellers.length === 0 && (
                  <div className="col-span-full py-12 text-center text-zinc-500 border border-zinc-200">
                     {t.home.no_best_sellers}
                  </div>
                )}
              </>
            )}
            </div>
        )}
      </section>

      {/* 4. Soft Vibrant Promo Banner */}
      <section className="mx-auto w-full max-w-7xl px-6 lg:px-8 py-12">
        <div className="relative overflow-hidden rounded-md bg-gradient-to-br from-rose-100 via-orange-50 to-amber-100 px-6 py-24 sm:px-12 sm:py-32 lg:px-20 text-center shadow-lg border border-rose-200/50">
          {/* Background decorative elements */}
          <div className="absolute top-0 left-0 w-full h-full overflow-hidden opacity-40 pointer-events-none">
            <div className="absolute -top-24 -left-24 w-96 h-96 rounded-full bg-white blur-3xl"></div>
            <div className="absolute -bottom-24 -right-24 w-96 h-96 rounded-full bg-rose-200 blur-3xl"></div>
          </div>
          
          <div className="relative z-10">
            <Sparkles className="mx-auto mb-6 h-12 w-12 text-rose-400 animate-pulse" strokeWidth={1.5} />
            <h2 className="text-4xl font-black tracking-tighter text-zinc-900 sm:text-6xl mb-6 uppercase">
              Mega <span className="font-light text-rose-500">Sale</span> Event
            </h2>
            <p className="mx-auto max-w-2xl text-zinc-600 mb-10 text-lg sm:text-xl font-medium">
              Giảm giá lên đến 50% cho các sản phẩm tuyển chọn. Làm mới phong cách của bạn với bộ sưu tập rạng rỡ nhất mùa này.
            </p>
            <Link href="/flash-sales">
              <Button size="lg" className="rounded-md border-0 bg-rose-500 text-white hover:bg-rose-600 px-12 h-14 tracking-widest uppercase text-sm font-bold shadow-md transition-all hover:scale-105 active:scale-95">
                Khám phá ngay
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* 5. New Arrivals */}
      <section className="mx-auto w-full max-w-7xl px-6 lg:px-8 py-24">
        <div className="flex items-end justify-between mb-12 border-b border-black pb-4">
          <h2 className="text-2xl font-bold uppercase tracking-wide text-black">{t.home.new_arrivals_title}</h2>
          <Link href="/products" className="text-sm font-medium text-zinc-500 hover:text-black uppercase tracking-widest transition-colors">
            {t.home.view_all_arrow}
          </Link>
        </div>

        {loading ? (
             <div className="grid grid-cols-1 gap-10 sm:grid-cols-2 lg:grid-cols-4">
                {[...Array(8)].map((_, i) => (
                    <div key={i} className="flex flex-col space-y-4">
                        <Skeleton className="h-[400px] w-full rounded-md" />
                        <div className="space-y-2">
                            <Skeleton className="h-4 w-3/4 rounded-md" />
                            <Skeleton className="h-4 w-1/4 rounded-md" />
                        </div>
                    </div>
                ))}
            </div>
        ) : (
            <div className="grid grid-cols-1 gap-10 sm:grid-cols-2 lg:grid-cols-4">
              {loadError ? (
                renderErrorState()
              ) : (
                newArrivals.map((product) => (
                  <ProductCard key={product.id} product={product} />
                ))
              )}
            </div>
        )}
        
        <div className="mt-16 text-center">
            <Link href="/products">
                <Button variant="outline" size="lg" className="rounded-md border-black text-black hover:bg-black hover:text-white px-12 tracking-widest uppercase text-sm">
                    {t.home.load_more_products}
                </Button>
            </Link>
        </div>
      </section>

      {/* 6. Benefits Footer */}
      <section className="border-t border-zinc-100 bg-white py-16">
        <div className="mx-auto max-w-7xl px-6 lg:px-8">
          <div className="grid grid-cols-1 gap-8 md:grid-cols-3">
            {Benefits.map((item, idx) => (
              <div key={idx} className="flex flex-col items-center text-center p-6">
                <item.icon size={28} strokeWidth={1} className="mb-4 text-black" />
                <h3 className="font-bold text-black uppercase tracking-wider text-sm mb-2">{item.title}</h3>
                <p className="text-sm text-zinc-500 font-light">{item.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

    </div>
  );
}
