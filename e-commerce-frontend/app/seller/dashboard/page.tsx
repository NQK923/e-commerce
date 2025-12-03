'use client';

import React from "react";
import Link from "next/link";
import { Package2, ShoppingBag, TrendingUp, Wallet, Plus, Sparkles } from "lucide-react";
import { productApi } from "@/src/api/productApi";
import { Button } from "@/src/components/ui/button";
import { Spinner } from "@/src/components/ui/spinner";
import { useRequireAuth } from "@/src/hooks/use-require-auth";
import { Product } from "@/src/types/product";

export default function SellerDashboardPage() {
  const { user, initializing } = useRequireAuth("/login");
  const [products, setProducts] = React.useState<Product[]>([]);
  const [loading, setLoading] = React.useState(false);

  React.useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const res = await productApi.list({ page: 0, size: 12 });
        setProducts(res.items ?? []);
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, []);

  const totalProducts = products.length;
  const totalValue = products.reduce((sum, p) => sum + (p.price ?? 0), 0);
  const avgPrice = totalProducts ? Math.round((totalValue / totalProducts) * 100) / 100 : 0;

  if (initializing || !user) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        Loading dashboard...
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 space-y-8">
      <header className="flex flex-col gap-3 rounded-3xl bg-gradient-to-r from-emerald-600 to-emerald-700 p-6 text-white shadow-lg sm:flex-row sm:items-center sm:justify-between">
        <div className="space-y-2">
          <div className="inline-flex items-center gap-2 rounded-full bg-white/15 px-3 py-1 text-xs font-semibold">
            <Sparkles size={14} />
            Seller Dashboard
          </div>
          <h1 className="text-3xl font-extrabold">Xin chào, {user.displayName ?? user.email}</h1>
          <p className="text-emerald-100">Quản lý sản phẩm và theo dõi hiệu suất gian hàng của bạn.</p>
        </div>
        <div className="flex gap-3">
          <Link href="/seller/products/new">
            <Button variant="secondary" className="bg-white text-emerald-700 hover:bg-emerald-50">
              <Plus size={16} className="mr-2" />
              Thêm sản phẩm
            </Button>
          </Link>
        </div>
      </header>

      <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {[
          { icon: Package2, label: "Tổng sản phẩm", value: totalProducts },
          { icon: Wallet, label: "Giá trị danh mục", value: `${totalValue.toLocaleString()} ${products[0]?.currency ?? "VND"}` },
          { icon: TrendingUp, label: "Giá trung bình", value: `${avgPrice.toLocaleString()} ${products[0]?.currency ?? "VND"}` },
          { icon: ShoppingBag, label: "Đơn gần đây", value: "—" },
        ].map((item) => (
          <div key={item.label} className="flex items-center gap-3 rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm">
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-emerald-50 text-emerald-700">
              <item.icon size={20} />
            </div>
            <div>
              <p className="text-sm text-zinc-500">{item.label}</p>
              <p className="text-xl font-bold text-zinc-900">{item.value}</p>
            </div>
          </div>
        ))}
      </section>

      <section className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm font-semibold text-emerald-700">Danh sách sản phẩm</p>
            <h2 className="text-2xl font-bold text-zinc-900">Products</h2>
          </div>
          <Link href="/seller/register">
            <Button variant="outline" size="sm">Yêu cầu hỗ trợ</Button>
          </Link>
        </div>

        {loading ? (
          <div className="mt-6 flex h-40 items-center justify-center gap-3 text-sm text-zinc-500">
            <Spinner />
            Đang tải sản phẩm...
          </div>
        ) : (
          <div className="mt-4 overflow-x-auto rounded-xl border border-zinc-100">
            <table className="min-w-full text-left text-sm">
              <thead className="bg-zinc-50 text-zinc-500">
                <tr>
                  <th className="px-4 py-3 font-semibold">Sản phẩm</th>
                  <th className="px-4 py-3 font-semibold">Danh mục</th>
                  <th className="px-4 py-3 font-semibold">Giá</th>
                  <th className="px-4 py-3 font-semibold">Tồn kho</th>
                  <th className="px-4 py-3 font-semibold">Hành động</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-zinc-100">
                {products.map((product) => (
                  <tr key={product.id} className="hover:bg-zinc-50">
                    <td className="px-4 py-3">
                      <div className="font-semibold text-zinc-900">{product.name}</div>
                      <div className="text-xs text-zinc-500 line-clamp-1">{product.description}</div>
                    </td>
                    <td className="px-4 py-3">{product.category ?? "Chưa phân loại"}</td>
                    <td className="px-4 py-3">{product.price} {product.currency ?? "VND"}</td>
                    <td className="px-4 py-3">{product.stock ?? "—"}</td>
                    <td className="px-4 py-3">
                      <div className="flex gap-2">
                        <Button size="sm" variant="outline">Sửa</Button>
                        <Button size="sm" variant="ghost" className="text-red-600 hover:text-red-700">Gỡ</Button>
                      </div>
                    </td>
                  </tr>
                ))}
                {!products.length && (
                  <tr>
                    <td colSpan={5} className="px-4 py-6 text-center text-sm text-zinc-500">
                      Chưa có sản phẩm nào. Hãy thêm sản phẩm đầu tiên!
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}
