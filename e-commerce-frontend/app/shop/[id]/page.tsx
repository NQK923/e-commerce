'use client';

import { useParams } from "next/navigation";
import React, { useEffect, useState } from "react";
import Link from "next/link";
import { MessageSquare, Calendar, Star, Store } from "lucide-react";

import { userApi } from "@/src/api/userApi";
import { productApi } from "@/src/api/productApi";
import { ProductCard } from "@/src/components/product/product-card";
import { Button } from "@/src/components/ui/button";
import { Spinner } from "@/src/components/ui/spinner";
import { User } from "@/src/types/auth";
import { Product } from "@/src/types/product";
import { useAuth } from "@/src/store/auth-store";

export default function ShopPage() {
  const params = useParams<{ id: string }>();
  const sellerId = params?.id;
  const { user: currentUser } = useAuth();
  
  const [seller, setSeller] = useState<User | null>(null);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!sellerId) return;
    const load = async () => {
      setLoading(true);
      try {
        const [sellerData, productsData] = await Promise.all([
          userApi.getById(sellerId),
          productApi.list({ sellerId: sellerId, size: 20 })
        ]);
        setSeller(sellerData);
        setProducts(productsData.items);
      } catch (err) {
        console.error(err);
        setError("Failed to load shop information");
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, [sellerId]);

  if (loading) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        Loading Shop...
      </div>
    );
  }

  if (error || !seller) {
    return (
      <div className="mx-auto flex max-w-3xl flex-col items-center gap-3 px-4 py-10 text-center">
        <p className="text-lg font-semibold text-zinc-900">Shop not found</p>
        <Button variant="secondary" onClick={() => window.location.reload()}>
          Retry
        </Button>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-zinc-50 pb-20">
        {/* Shop Header */}
        <div className="bg-white border-b border-zinc-200">
            <div className="mx-auto max-w-7xl px-4 py-8">
                <div className="flex flex-col md:flex-row items-center gap-6">
                    <div className="h-24 w-24 overflow-hidden rounded-full border border-zinc-200 bg-zinc-100">
                        {/* eslint-disable-next-line @next/next/no-img-element */}
                        <img 
                            src={seller.avatarUrl || "https://placehold.co/200?text=Shop"} 
                            alt={seller.displayName}
                            className="h-full w-full object-cover"
                        />
                    </div>
                    <div className="flex-1 text-center md:text-left">
                        <h1 className="text-2xl font-bold text-zinc-900">{seller.displayName}</h1>
                        <div className="mt-2 flex flex-wrap justify-center md:justify-start gap-4 text-sm text-zinc-500">
                            <div className="flex items-center gap-1">
                                <Store size={16} />
                                <span>{products.length} Sản phẩm</span>
                            </div>
                            {/* Placeholder for rating - could be calculated if we had aggregation */}
                            <div className="flex items-center gap-1">
                                <Star size={16} className="text-amber-400" />
                                <span>4.9 Đánh giá</span>
                            </div>
                            <div className="flex items-center gap-1">
                                <Calendar size={16} />
                                <span>Tham gia 2024</span>
                            </div>
                        </div>
                    </div>
                    <div className="flex gap-3">
                         {currentUser?.id !== seller.id && (
                            <Link href={`/chat?userId=${seller.id}`}>
                                <Button className="bg-emerald-600 hover:bg-emerald-700">
                                    <MessageSquare size={18} className="mr-2" />
                                    Chat ngay
                                </Button>
                            </Link>
                         )}
                         <Button variant="outline">Theo dõi</Button>
                    </div>
                </div>
            </div>
        </div>

        {/* Shop Products */}
        <div className="mx-auto max-w-7xl px-4 py-8">
            <h2 className="mb-6 text-xl font-bold text-zinc-900">Sản phẩm của Shop</h2>
            
            {products.length > 0 ? (
                <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
                    {products.map((p) => (
                        <ProductCard key={p.id} product={p} />
                    ))}
                </div>
            ) : (
                <div className="text-center py-20 text-zinc-500">
                    Shop này chưa có sản phẩm nào.
                </div>
            )}
        </div>
    </div>
  );
}
