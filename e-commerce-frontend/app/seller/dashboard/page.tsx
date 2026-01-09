'use client';

import React, { Suspense, useMemo, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { 
  Package2, ShoppingBag, TrendingUp, Wallet, Plus, Sparkles, 
  Trash2, ArrowRight, BarChart3
} from "lucide-react";
import { 
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer 
} from 'recharts';

import { productApi } from "@/src/api/productApi";
import { orderApi } from "@/src/api/orderApi";
import { Order } from "@/src/types/order";
import { Button } from "@/src/components/ui/button";
import { Spinner } from "@/src/components/ui/spinner";
import { Card } from "@/src/components/ui/card";
import { useRequireAuth } from "@/src/hooks/use-require-auth";
import { Product } from "@/src/types/product";
import { useToast } from "@/src/components/ui/toast-provider";
import { useTranslation } from "@/src/providers/language-provider";

// Helper to format currency
const formatMoney = (amount: number, currency = "VND") => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency }).format(amount);
};

function SellerDashboardContent() {
  const { user, initializing } = useRequireAuth("/login");
  const router = useRouter();
  const [products, setProducts] = useState<Product[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [processingId, setProcessingId] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'selling' | 'out_of_stock'>('selling');
  const { addToast } = useToast();
  const { t } = useTranslation();

  React.useEffect(() => {
    if (!user || !user.roles?.includes("SELLER")) return;
    
    const loadData = async () => {
      const sellerId = user.id;
      if (!sellerId) return;
      try {
        const [prodRes, ordRes] = await Promise.all([
          productApi.list({ page: 0, size: 100, includeOutOfStock: true, sellerId }),
          orderApi.list({ page: 0, size: 50, sort: 'createdAt,desc', sellerId })
        ]);
        const scopedProducts = (prodRes.items ?? []).filter((p) => p.sellerId === sellerId);
        setProducts(scopedProducts);
        setOrders(ordRes.items ?? []);
      } catch (e) {
        console.error("Failed to load dashboard data", e);
        addToast(t.seller.dashboard.failed_load, "error");
      }
    };
      
    void loadData();
    }, [user, addToast, t]);

  React.useEffect(() => {
    if (initializing) return;
    if (user && !user.roles?.includes("SELLER")) {
      router.replace("/seller/register?next=/seller/dashboard");
    }
  }, [initializing, router, user]);

  // Calculations
  const totalProducts = products.length;
  // Calculate revenue from loaded orders
  const totalRevenue = useMemo(() => orders.reduce((sum, o) => sum + (o.total || 0), 0), [orders]);
  // Use totalElements from API if we had it, but for now use loaded length + '...' if full
  const totalOrdersDisplay = orders.length >= 50 ? "50+" : orders.length;

  const chartData = useMemo(() => {
    if (orders.length === 0) return [];
    const groups: Record<string, number> = {};
    // Process orders oldest to newest for the chart
    [...orders].reverse().forEach(o => {
      const date = new Date(o.createdAt).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' });
      groups[date] = (groups[date] || 0) + (o.total || 0);
    });
    return Object.entries(groups).map(([date, value]) => ({ date, value }));
  }, [orders]);

  const topSellingProducts = useMemo(() => {
    return [...products]
      .sort((a, b) => (b.soldCount ?? 0) - (a.soldCount ?? 0))
      .slice(0, 5);
  }, [products]);

  const displayedProducts = useMemo(() => {
    return products.filter(p => {
        if (activeTab === 'selling') return (p.stock ?? 0) > 0;
        return (p.stock ?? 0) <= 0;
    });
  }, [products, activeTab]);

  const handleEdit = (productId: string) => {
    router.push(`/seller/products/new?productId=${productId}`);
  };

  const handleRemove = async (product: Product) => {
    if (!user) return;
    if (!confirm(t.seller.dashboard.confirm_remove.replace("{{name}}", product.name))) return;
    setProcessingId(product.id);
    try {
      await productApi.update(product.id, {
        name: product.name,
        description: product.description,
        price: product.price,
        currency: product.currency ?? "VND",
        quantity: 0,
        categoryId: product.category,
        sellerId: user.id,
        images: product.images?.map((img, idx) => ({
            url: img.url,
            primary: img.primary ?? idx === 0,
          })) ?? [],
      });
      // Update local state to reflect change (mark as 0 stock)
      setProducts((prev) => prev.map((p) => p.id === product.id ? { ...p, stock: 0 } : p));
      addToast(t.seller.dashboard.update_success, "success");
    } catch (error) {
      console.error("Failed to remove product", error);
      addToast(t.seller.dashboard.failed_update, "error");
    } finally {
      setProcessingId(null);
    }
  };

  if (initializing || !user) {
    return (
      <div className="flex min-h-[60vh] flex-col items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner size="lg" />
        <span className="animate-pulse">{t.seller.dashboard.loading}</span>
      </div>
    );
  }

  if (!user.roles?.includes("SELLER")) {
    return null; // Will redirect in useEffect
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 space-y-8 bg-zinc-50/30 min-h-screen">
      {/* Header */}
      <header className="flex flex-col gap-6 rounded-3xl bg-gradient-to-br from-emerald-900 via-emerald-800 to-teal-900 p-8 text-white shadow-2xl shadow-emerald-900/20 sm:flex-row sm:items-center sm:justify-between relative overflow-hidden">
        {/* Abstract Background Overlay */}
        <div className="absolute inset-0 opacity-10 pointer-events-none">
            <svg className="h-full w-full" viewBox="0 0 100 100" preserveAspectRatio="none">
              <path d="M0 100 C 20 0 50 0 100 100 Z" fill="white" />
            </svg>
        </div>
        
        <div className="space-y-2 relative z-10">
          <div className="inline-flex items-center gap-2 rounded-full bg-white/10 px-3 py-1 text-xs font-semibold backdrop-blur-md border border-white/10">
            <Sparkles size={14} className="text-yellow-300" />
            Seller Dashboard
          </div>
          <h1 className="text-3xl font-bold tracking-tight">{t.seller.dashboard.welcome.replace("{{name}}", user.displayName ?? "Seller")}</h1>
          <p className="text-emerald-100/80 font-medium">{t.seller.dashboard.overview}</p>
        </div>
        <div className="flex gap-3 relative z-10">
          <Link href="/seller/settings">
             <Button className="bg-emerald-800/50 hover:bg-emerald-800 text-white border border-emerald-400/30 backdrop-blur-sm">
                Settings
             </Button>
          </Link>
          <Link href="/seller/products/new">
            <Button size="lg" className="bg-white text-emerald-900 font-bold shadow-xl hover:bg-emerald-50 border-0 transition-transform hover:-translate-y-0.5">
              <Plus size={20} className="mr-2" />
              {t.seller.dashboard.add_product}
            </Button>
          </Link>
        </div>
      </header>

      {/* Stats Cards */}
      <section className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {[
          { label: t.seller.dashboard.revenue, value: formatMoney(totalRevenue), sub: t.seller.dashboard.recent_50_orders, icon: Wallet, color: "text-emerald-600", bg: "bg-emerald-100" },
          { label: t.seller.dashboard.total_orders, value: totalOrdersDisplay, sub: t.seller.dashboard.orders_label, icon: ShoppingBag, color: "text-blue-600", bg: "bg-blue-100" },
          { label: t.seller.dashboard.products_label, value: totalProducts, sub: t.seller.dashboard.selling_label, icon: Package2, color: "text-purple-600", bg: "bg-purple-100" },
          { label: t.seller.dashboard.avg_order, value: orders.length ? formatMoney(totalRevenue / orders.length) : "0 ₫", sub: t.seller.dashboard.avg_value, icon: TrendingUp, color: "text-amber-600", bg: "bg-amber-100" },
        ].map((stat, i) => (
          <Card key={i} className="p-6 flex items-center gap-5 border-zinc-100 shadow-sm hover:shadow-md transition-shadow">
            <div className={`flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl ${stat.bg} ${stat.color}`}>
              <stat.icon size={28} />
            </div>
            <div>
              <p className="text-sm font-medium text-zinc-500">{stat.label}</p>
              <p className="text-2xl font-bold text-zinc-900 tracking-tight">{stat.value}</p>
              <p className="text-xs text-zinc-400 mt-0.5">{stat.sub}</p>
            </div>
          </Card>
        ))}
      </section>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Main Chart Section */}
        <section className="lg:col-span-2 space-y-6">
          <Card className="p-6 border-zinc-200 shadow-sm">
            <div className="mb-8 flex items-center justify-between">
              <div>
                <h3 className="text-lg font-bold text-zinc-900">{t.seller.dashboard.revenue_chart}</h3>
                <p className="text-sm text-zinc-500">{t.seller.dashboard.revenue_trend}</p>
              </div>
              <div className="rounded-lg bg-zinc-50 p-2 text-zinc-400">
                <BarChart3 size={20} />
              </div>
            </div>
            <div className="h-[320px] w-full">
              {chartData.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={chartData} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                    <defs>
                      <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#059669" stopOpacity={0.1}/>
                        <stop offset="95%" stopColor="#059669" stopOpacity={0}/>
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f4f4f5" />
                    <XAxis 
                      dataKey="date" 
                      axisLine={false} 
                      tickLine={false} 
                      tick={{ fontSize: 12, fill: '#71717a' }}
                      dy={10}
                    />
                    <YAxis 
                      axisLine={false} 
                      tickLine={false} 
                      tick={{ fontSize: 12, fill: '#71717a' }}
                      tickFormatter={(value) => `${value / 1000}k`}
                    />
                    <Tooltip 
                      contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 10px 30px -10px rgba(0,0,0,0.15)' }}
                      formatter={(value: number) => [formatMoney(value), t.seller.dashboard.revenue]}
                      cursor={{ stroke: '#10b981', strokeWidth: 1, strokeDasharray: '4 4' }}
                    />
                    <Area 
                      type="monotone" 
                      dataKey="value" 
                      stroke="#059669" 
                      strokeWidth={3}
                      fillOpacity={1} 
                      fill="url(#colorRevenue)" 
                      activeDot={{ r: 6, fill: "#059669", stroke: "#fff", strokeWidth: 2 }}
                    />
                  </AreaChart>
                </ResponsiveContainer>
              ) : (
                <div className="flex h-full flex-col items-center justify-center text-zinc-400">
                  <BarChart3 size={48} className="mb-2 opacity-20" />
                  <p>{t.seller.dashboard.no_revenue_data}</p>
                </div>
              )}
            </div>
          </Card>

          {/* Recent Orders Table */}
          <Card className="overflow-hidden border-zinc-200 shadow-sm">
            <div className="flex items-center justify-between border-b border-zinc-100 bg-zinc-50/50 p-6">
              <h3 className="font-bold text-zinc-900">{t.seller.dashboard.recent_orders}</h3>
              <Link href="/seller/orders" className="flex items-center text-sm font-medium text-emerald-600 hover:text-emerald-700 hover:underline">
                {t.seller.dashboard.view_all} <ArrowRight size={16} className="ml-1" />
              </Link>
            </div>
            <div className="overflow-x-auto">
              <table className="min-w-full text-left text-sm">
                <thead className="bg-zinc-50 text-zinc-500">
                  <tr>
                    <th className="px-6 py-3 font-semibold">{t.seller.dashboard.order_id}</th>
                    <th className="px-6 py-3 font-semibold">{t.seller.dashboard.created_at}</th>
                    <th className="px-6 py-3 font-semibold">{t.seller.dashboard.total_amount}</th>
                    <th className="px-6 py-3 font-semibold">{t.seller.dashboard.status}</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-zinc-100">
                  {orders.slice(0, 5).map((order) => (
                    <tr key={order.id} className="hover:bg-zinc-50 transition-colors">
                      <td className="px-6 py-4 font-mono text-xs text-zinc-500">#{order.id.slice(0, 8)}</td>
                      <td className="px-6 py-4 text-zinc-600">
                        {new Date(order.createdAt).toLocaleDateString('vi-VN')}
                      </td>
                      <td className="px-6 py-4 font-medium text-zinc-900">
                        {formatMoney(order.total, order.currency)}
                      </td>
                      <td className="px-6 py-4">
                        <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium border
                          ${order.status === 'PAID' ? 'bg-emerald-50 text-emerald-700 border-emerald-100' : 
                            order.status === 'PENDING' ? 'bg-blue-50 text-blue-700 border-blue-100' : 
                            order.status === 'SHIPPING' ? 'bg-amber-50 text-amber-700 border-amber-100' :
                            order.status === 'DELIVERED' ? 'bg-zinc-50 text-zinc-700 border-zinc-200' :
                            order.status === 'RETURNED' ? 'bg-zinc-50 text-zinc-700 border-zinc-200' :
                            order.status === 'CANCELLED' ? 'bg-red-50 text-red-700 border-red-100' : 'bg-zinc-100 text-zinc-700 border-zinc-200'
                          }`}>
                          {order.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                  {!orders.length && (
                    <tr>
                      <td colSpan={4} className="p-12 text-center text-zinc-500">{t.seller.dashboard.no_orders}</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </Card>
        </section>

        {/* Right Column: Top Products & Quick Actions */}
        <aside className="space-y-6">
           {/* Top Selling Products */}
           <Card className="overflow-hidden border-zinc-200 shadow-sm">
             <div className="border-b border-zinc-100 bg-zinc-50/50 p-5">
               <h3 className="font-bold text-zinc-900">{t.seller.dashboard.top_products}</h3>
             </div>
             <div className="divide-y divide-zinc-100">
               {topSellingProducts.length > 0 ? (
                 topSellingProducts.map((p, i) => (
                   <div key={p.id} className="flex items-center gap-4 p-4 hover:bg-zinc-50 transition-colors">
                      <div className="flex h-8 w-8 items-center justify-center rounded-full bg-emerald-100 text-sm font-bold text-emerald-700">
                        {i + 1}
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="truncate font-medium text-zinc-900">{p.name}</p>
                        <p className="text-xs text-zinc-500">{t.seller.dashboard.sold_count.replace("{{count}}", (p.soldCount ?? 0).toString())}</p>
                      </div>
                      <div className="text-sm font-semibold text-zinc-900">
                        {formatMoney(p.price, p.currency)}
                      </div>
                   </div>
                 ))
               ) : (
                  <div className="p-8 text-center text-sm text-zinc-500">{t.seller.dashboard.no_sales_data}</div>
               )}
             </div>
           </Card>

           {/* Quick Stats or Tips */}
           <Card className="bg-gradient-to-br from-indigo-500 to-purple-600 text-white p-6 shadow-lg">
              <h3 className="font-bold text-lg mb-2">{t.seller.dashboard.sales_tips}</h3>
              <ul className="space-y-2 text-sm text-indigo-100 list-disc list-inside">
                 <li>{t.seller.dashboard.tip_1}</li>
                 <li>{t.seller.dashboard.tip_2}</li>
                 <li>{t.seller.dashboard.tip_3}</li>
              </ul>
           </Card>
        </aside>
      </div>

      {/* Products Table with Tabs */}
      <Card className="overflow-hidden border-zinc-200 shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between border-b border-zinc-100 bg-zinc-50/50 p-4 gap-4">
          <div className="flex gap-1 bg-zinc-100/80 p-1 rounded-lg">
              <button
                onClick={() => setActiveTab('selling')}
                className={`px-4 py-1.5 text-sm font-medium rounded-md transition-all ${
                    activeTab === 'selling' 
                        ? 'bg-white text-zinc-900 shadow-sm' 
                        : 'text-zinc-500 hover:text-zinc-700 hover:bg-zinc-200/50'
                }`}
              >
                  {t.seller.dashboard.selling_tab}
              </button>
              <button
                onClick={() => setActiveTab('out_of_stock')}
                className={`px-4 py-1.5 text-sm font-medium rounded-md transition-all ${
                    activeTab === 'out_of_stock' 
                        ? 'bg-white text-zinc-900 shadow-sm' 
                        : 'text-zinc-500 hover:text-zinc-700 hover:bg-zinc-200/50'
                }`}
              >
                  {t.seller.dashboard.out_of_stock_tab}
              </button>
          </div>
          <Button variant="outline" size="sm" onClick={() => router.push('/seller/products')}>
            {t.seller.dashboard.manage_all}
          </Button>
        </div>

        <div className="overflow-x-auto">
          <table className="min-w-full text-left text-sm">
            <thead className="bg-zinc-50 text-zinc-500">
              <tr>
                <th className="px-6 py-3 font-semibold">{t.seller.dashboard.product_col}</th>
                <th className="px-6 py-3 font-semibold">{t.seller.dashboard.category_col}</th>
                <th className="px-6 py-3 font-semibold">{t.seller.dashboard.price_col}</th>
                <th className="px-6 py-3 font-semibold">{t.seller.dashboard.stock_col}</th>
                <th className="px-6 py-3 font-semibold">{t.seller.dashboard.action_col}</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {displayedProducts.slice(0, 10).map((product) => (
                <tr key={product.id} className="hover:bg-zinc-50 transition-colors">
                  <td className="px-6 py-3 max-w-xs">
                    <div className="font-semibold text-zinc-900 truncate" title={product.name}>{product.name}</div>
                    <div className="text-xs text-zinc-500 truncate">{product.description}</div>
                  </td>
                  <td className="px-6 py-3">
                     <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-zinc-100 text-zinc-800">
                        {product.category ?? t.seller.dashboard.others}
                     </span>
                  </td>
                  <td className="px-6 py-3 font-medium text-zinc-700">{formatMoney(product.price, product.currency)}</td>
                  <td className="px-6 py-3">
                    <span className={!product.stock ? "text-red-600 font-bold bg-red-50 px-2 py-1 rounded-full text-xs" : "text-zinc-700"}>
                      {product.stock ? product.stock : t.seller.dashboard.out_of_stock}
                    </span>
                  </td>
                  <td className="px-6 py-3">
                    <div className="flex gap-2">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => handleEdit(product.id)}
                        className="h-8 px-3 text-zinc-600 hover:text-emerald-700 hover:border-emerald-200 hover:bg-emerald-50"
                      >
                        {t.seller.dashboard.edit}
                      </Button>
                      <Button
                        size="sm"
                        variant="ghost"
                        className="h-8 w-8 p-0 text-zinc-400 hover:text-red-600 hover:bg-red-50"
                        onClick={() => handleRemove(product)}
                        disabled={processingId === product.id}
                        title={t.seller.dashboard.remove_tooltip}
                      >
                        {processingId === product.id ? <Spinner size="sm" /> : <Trash2 size={16} />}
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
              {!displayedProducts.length && (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center text-zinc-500 flex flex-col items-center justify-center">
                    <Package2 size={32} className="mb-2 opacity-20" />
                    {activeTab === 'selling' ? t.seller.dashboard.no_products_selling : t.seller.dashboard.no_products_out_of_stock}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
}

export default function SellerDashboardPage() {
  const { t } = useTranslation();
  return (
    <Suspense fallback={
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        {t.seller.dashboard.loading}
      </div>
    }>
      <SellerDashboardContent />
    </Suspense>
  );
}