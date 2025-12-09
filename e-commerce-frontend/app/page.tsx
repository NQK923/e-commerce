'use client';

import Link from "next/link";
import React, { useEffect, useState } from "react";
import Image from "next/image";
import {
  ArrowRight,
  ShieldCheck,
  Truck,
  Clock,
  Smartphone,
  Shirt,
  HomeIcon,
  BookOpen,
  Sparkles,
  Zap,
  Star
} from "lucide-react";
import { productApi } from "@/src/api/productApi";
import { ProductCard } from "@/src/components/product/product-card";
import { Button } from "@/src/components/ui/button";
import { useCart } from "@/src/store/cart-store";
import { useToast } from "@/src/components/ui/toast-provider";
import { Product } from "@/src/types/product";
import { useTranslation } from "@/src/providers/language-provider";
import { Skeleton } from "@/src/components/ui/skeleton";

export default function HomePage() {
  const { addItem } = useCart();
  const { addToast } = useToast();
  const { t } = useTranslation();

  const [bestSellers, setBestSellers] = useState<Product[]>([]);
  const [newArrivals, setNewArrivals] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      try {
        const [bestSellerData, newArrivalData] = await Promise.all([
          productApi.list({ size: 4, sort: "soldCount,desc", page: 0 }),
          productApi.list({ size: 8, sort: "createdAt,desc", page: 0 })
        ]);
        setBestSellers(bestSellerData.items);
        setNewArrivals(newArrivalData.items);
      } catch (error) {
        console.error(error);
        // Silent fail or toast
      } finally {
        setLoading(false);
      }
    };
    void loadData();
  }, []);

  const handleAddToCart = async (product: Product) => {
    await addItem(product, 1);
    addToast(`${product.name} ${t.cart.added}`, "success");
  };

  const Categories = [
    { name: t.home.categories.electronics || "Điện tử", icon: Smartphone, color: "text-blue-600", bg: "bg-blue-50", href: "electronics" },
    { name: t.home.categories.fashion || "Thời trang", icon: Shirt, color: "text-rose-600", bg: "bg-rose-50", href: "fashion" },
    { name: t.home.categories.home || "Nhà cửa", icon: HomeIcon, color: "text-amber-600", bg: "bg-amber-50", href: "home" },
    { name: t.home.categories.books || "Sách", icon: BookOpen, color: "text-indigo-600", bg: "bg-indigo-50", href: "books" },
  ];

  const Benefits = [
    { icon: Truck, title: t.home.features.shipping, desc: "Miễn phí vận chuyển cho đơn từ 500k" },
    { icon: ShieldCheck, title: t.home.features.payment, desc: "Thanh toán bảo mật 100%" },
    { icon: Clock, title: t.home.features.support, desc: "Hỗ trợ khách hàng 24/7" },
    { icon: Star, title: "Chất lượng", desc: "Cam kết sản phẩm chính hãng" },
  ];

  return (
    <div className="flex flex-col gap-16 pb-20 bg-zinc-50/50">
      
      {/* 1. Hero Section */}
      <section className="relative overflow-hidden bg-emerald-900 pt-16 pb-20 lg:pt-24">
        {/* Abstract Background Pattern */}
        <div className="absolute inset-0 opacity-10">
            <svg className="h-full w-full" viewBox="0 0 100 100" preserveAspectRatio="none">
              <path d="M0 100 C 20 0 50 0 100 100 Z" fill="white" />
            </svg>
        </div>
        
        <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="grid gap-12 lg:grid-cols-2 lg:gap-8 items-center">
            <div className="flex flex-col items-center text-center lg:items-start lg:text-left">
              <div className="mb-6 inline-flex items-center rounded-full bg-emerald-800/50 px-3 py-1 text-sm font-medium text-emerald-100 border border-emerald-700/50 backdrop-blur-sm">
                <Sparkles className="mr-2 h-4 w-4 text-yellow-400" />
                <span>Chào mừng mùa mua sắm mới</span>
              </div>
              <h1 className="text-4xl font-extrabold tracking-tight text-white sm:text-6xl mb-6 leading-tight">
                Khám phá thế giới <br/>
                <span className="text-transparent bg-clip-text bg-gradient-to-r from-emerald-200 to-teal-400">
                  Mua sắm trực tuyến
                </span>
              </h1>
              <p className="text-lg text-emerald-100/80 mb-8 max-w-lg">
                Hàng ngàn sản phẩm chất lượng với mức giá ưu đãi đang chờ đón bạn. Trải nghiệm mua sắm tiện lợi và nhanh chóng ngay hôm nay.
              </p>
              <div className="flex flex-wrap gap-4 justify-center lg:justify-start">
                <Link href="/products">
                  <Button size="lg" className="h-14 px-8 text-lg bg-white text-emerald-900 hover:bg-emerald-50 rounded-full font-bold shadow-xl shadow-emerald-900/20">
                    Mua sắm ngay <ArrowRight className="ml-2 h-5 w-5" />
                  </Button>
                </Link>
                <Link href="/products?category=sale">
                  <Button size="lg" variant="outline" className="h-14 px-8 text-lg border-emerald-200/30 text-emerald-100 hover:bg-white/10 hover:text-white rounded-full bg-transparent">
                    Xem khuyến mãi
                  </Button>
                </Link>
              </div>
            </div>
            
            {/* Hero Image / Illustration Placeholder */}
            <div className="relative hidden lg:block">
               {/* Decorative circles */}
               <div className="absolute top-0 right-0 -mr-20 -mt-20 h-96 w-96 rounded-full bg-emerald-500/20 blur-3xl" />
               <div className="absolute bottom-0 left-0 -ml-20 -mb-20 h-96 w-96 rounded-full bg-teal-500/20 blur-3xl" />
               
               <div className="relative rounded-2xl bg-white/5 border border-white/10 p-6 backdrop-blur-sm shadow-2xl rotate-3 hover:rotate-0 transition-transform duration-500">
                  <div className="aspect-[4/3] w-full rounded-lg bg-gradient-to-br from-zinc-100 to-zinc-200 flex items-center justify-center overflow-hidden relative">
                    {/* Placeholder for Hero Image */}
                    <div className="text-zinc-400 font-medium flex flex-col items-center">
                        <Image 
                          src="https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?q=80&w=2070&auto=format&fit=crop" 
                          alt="Shopping" 
                          fill
                          className="object-cover opacity-90"
                        />
                    </div>
                  </div>
                  <div className="mt-4 flex items-center justify-between">
                     <div className="h-2 w-24 rounded bg-white/20" />
                     <div className="h-8 w-24 rounded bg-emerald-500" />
                  </div>
               </div>
            </div>
          </div>
        </div>
      </section>

      {/* 2. Benefits / Trust Signals */}
      <section className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 -mt-10 relative z-10">
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {Benefits.map((item, idx) => (
            <div key={idx} className="flex items-center gap-4 rounded-xl border border-zinc-100 bg-white p-6 shadow-lg shadow-zinc-200/50 transition-all hover:-translate-y-1 hover:shadow-xl">
              <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-emerald-50 text-emerald-600">
                <item.icon size={24} />
              </div>
              <div>
                <h3 className="font-bold text-zinc-900">{item.title}</h3>
                <p className="text-sm text-zinc-500">{item.desc}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* 3. Categories */}
      <section className="mx-auto w-full max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between mb-8">
            <h2 className="text-2xl font-bold text-zinc-900">Danh mục nổi bật</h2>
        </div>
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4 lg:gap-8">
          {Categories.map((cat, i) => (
            <Link 
              key={i} 
              href={`/products?category=${cat.href}`}
              className="group relative flex flex-col items-center justify-center overflow-hidden rounded-2xl bg-white p-8 text-center shadow-sm border border-zinc-100 transition-all hover:border-emerald-200 hover:shadow-md"
            >
              <div className={`mb-4 flex h-16 w-16 items-center justify-center rounded-full ${cat.bg} ${cat.color} transition-transform duration-300 group-hover:scale-110`}>
                <cat.icon size={32} />
              </div>
              <span className="text-lg font-medium text-zinc-900 group-hover:text-emerald-700">{cat.name}</span>
              <span className="mt-1 text-xs text-zinc-400 group-hover:text-emerald-500/70">Khám phá ngay</span>
            </Link>
          ))}
        </div>
      </section>

      {/* 4. Best Sellers (Bán chạy) */}
      <section className="mx-auto w-full max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between mb-8">
           <div className="flex items-center gap-2">
              <Zap className="h-6 w-6 text-amber-500 fill-amber-500 animate-pulse" />
              <h2 className="text-2xl font-bold text-zinc-900">Sản phẩm bán chạy</h2>
           </div>
           <Link href="/products?sort=soldCount,desc" className="text-sm font-medium text-emerald-600 hover:text-emerald-700 hover:underline">
              Xem tất cả &rarr;
           </Link>
        </div>

        {loading ? (
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
                {[...Array(4)].map((_, i) => (
                    <div key={i} className="flex flex-col space-y-3">
                        <Skeleton className="h-[300px] w-full rounded-xl" />
                        <div className="space-y-2">
                            <Skeleton className="h-4 w-[250px]" />
                            <Skeleton className="h-4 w-[200px]" />
                        </div>
                    </div>
                ))}
            </div>
        ) : (
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {bestSellers.map((product) => (
                <ProductCard key={product.id} product={product} onAddToCart={handleAddToCart} />
            ))}
             {bestSellers.length === 0 && (
                <div className="col-span-full py-12 text-center text-zinc-500 bg-white rounded-xl border border-dashed">
                   Chưa có dữ liệu sản phẩm bán chạy.
                </div>
             )}
            </div>
        )}
      </section>

      {/* 5. Promotional Banner */}
      <section className="mx-auto w-full max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="relative overflow-hidden rounded-3xl bg-zinc-900 text-white">
            <div className="absolute inset-0 bg-gradient-to-r from-purple-900 to-indigo-900 opacity-90" />
            <div className="absolute -right-20 -top-20 h-96 w-96 rounded-full bg-pink-500/30 blur-3xl" />
            
            <div className="relative grid items-center gap-8 px-8 py-12 lg:grid-cols-2 lg:px-16 lg:py-20">
                <div className="space-y-6">
                    <span className="inline-block rounded-lg bg-white/10 px-3 py-1 text-sm font-medium text-purple-200 backdrop-blur">
                        Khuyến mãi đặc biệt
                    </span>
                    <h2 className="text-3xl font-bold tracking-tight sm:text-4xl lg:text-5xl">
                        Giảm đến 50% cho <br/>
                        <span className="text-purple-300">Bộ sưu tập mùa hè</span>
                    </h2>
                    <p className="max-w-md text-lg text-purple-100/80">
                        Đừng bỏ lỡ cơ hội sở hữu những món đồ thời thượng với giá cực hời. Số lượng có hạn!
                    </p>
                    <Button size="lg" className="bg-white text-purple-900 hover:bg-purple-50 border-0 font-bold">
                        Mua ngay bây giờ
                    </Button>
                </div>
                {/* Decorative Element */}
                <div className="hidden lg:flex justify-end relative">
                     <div className="relative h-64 w-64 rounded-2xl bg-gradient-to-tr from-yellow-400 to-pink-500 rotate-6 shadow-2xl">
                         <div className="absolute inset-1 rounded-xl bg-black/20 backdrop-blur-sm flex items-center justify-center text-white font-bold text-4xl">
                             SALE
                         </div>
                     </div>
                     <div className="absolute -bottom-6 right-24 h-64 w-64 rounded-2xl bg-white/10 backdrop-blur-md -rotate-6 border border-white/20 shadow-xl" />
                </div>
            </div>
        </div>
      </section>

      {/* 6. New Arrivals (Gợi ý / Mới nhất) */}
      <section className="mx-auto w-full max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between mb-8">
          <h2 className="text-2xl font-bold text-zinc-900">Sản phẩm mới nhất</h2>
          <Link href="/products" className="text-sm font-medium text-emerald-600 hover:underline">
            Xem tất cả &rarr;
          </Link>
        </div>

        {loading ? (
             <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
                {[...Array(8)].map((_, i) => (
                    <div key={i} className="flex flex-col space-y-3">
                        <Skeleton className="h-[300px] w-full rounded-xl" />
                        <div className="space-y-2">
                            <Skeleton className="h-4 w-[250px]" />
                            <Skeleton className="h-4 w-[200px]" />
                        </div>
                    </div>
                ))}
            </div>
        ) : (
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {newArrivals.map((product) => (
              <ProductCard key={product.id} product={product} onAddToCart={handleAddToCart} />
            ))}
          </div>
        )}
        
        <div className="mt-12 text-center">
            <Link href="/products">
                <Button variant="outline" size="lg" className="min-w-[200px] border-zinc-300 bg-white hover:bg-zinc-50">
                    Xem thêm sản phẩm
                </Button>
            </Link>
        </div>
      </section>

    </div>
  );
}