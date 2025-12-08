'use client';

import React, { Suspense, useMemo } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { 
  Package2, ShoppingBag, TrendingUp, Wallet, Plus, Sparkles, 
  AlertTriangle, ArrowUpRight, Calendar
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

// Helper to format currency
const formatMoney = (amount: number, currency = "VND") => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency }).format(amount);
};

function SellerDashboardContent() {
  const { user, initializing } = useRequireAuth("/login");
  const router = useRouter();
  const [products, setProducts] = React.useState<Product[]>([]);
  const [orders, setOrders] = React.useState<Order[]>([]);
  const [loading, setLoading] = React.useState(true);
  const [actionProductId, setActionProductId] = React.useState<string | null>(null);
  const { addToast } = useToast();

  React.useEffect(() => {
    if (!user || !user.roles?.includes("SELLER")) return;
    
    const loadData = async () => {
      setLoading(true);
      try {
        const [prodRes, ordRes] = await Promise.all([
          productApi.list({ page: 0, size: 100, includeOutOfStock: true }),
          orderApi.list({ page: 0, size: 50, sort: 'createdAt,desc' })
        ]);
        setProducts(prodRes.items ?? []);
        setOrders(ordRes.items ?? []);
      } catch (e) {
        console.error("Failed to load dashboard data", e);
        addToast("Không tải được dữ liệu bảng điều khiển", "error");
      } finally {
        setLoading(false);
      }
    };
    
    void loadData();
  }, [user]);

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

  const lowStockProducts = useMemo(() => 
    products.filter(p => (p.stock ?? 0) < 5).slice(0, 5), 
  [products]);

  const chartData = useMemo(() => {
    const groups: Record<string, number> = {};
    // Process orders oldest to newest for the chart
    [...orders].reverse().forEach(o => {
      const date = new Date(o.createdAt).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' });
      groups[date] = (groups[date] || 0) + (o.total || 0);
    });
    return Object.entries(groups).map(([date, value]) => ({ date, value }));
  }, [orders]);

  const handleEdit = (productId: string) => {
    router.push(`/seller/products/new?productId=${productId}`);
  };

  const handleRemove = async (product: Product) => {
    if (!confirm(`Gỡ sản phẩm "${product.name}"?`)) return;
    setActionProductId(product.id);
    try {
      await productApi.update(product.id, {
        name: product.name,
        description: product.description,
        price: product.price,
        currency: product.currency ?? "VND",
        quantity: 0,
        categoryId: product.category,
        images: product.images?.map((img, idx) => ({
            url: img.url,
            primary: img.primary ?? idx === 0,
          })) ?? [],
      });
      setProducts((prev) => prev.filter((p) => p.id !== product.id));
      addToast("Đã gỡ sản phẩm khỏi danh sách", "success");
    } catch (error) {
      console.error("Failed to remove product", error);
      addToast("Không gỡ được sản phẩm", "error");
    } finally {
      setActionProductId(null);
    }
  };

  if (initializing || !user) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        Loading dashboard...
      </div>
    );
  }

  if (!user.roles?.includes("SELLER")) {
    return null; // Will redirect in useEffect
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 space-y-8">
      {/* Header */}
      <header className="flex flex-col gap-4 rounded-3xl bg-gradient-to-r from-emerald-600 to-emerald-800 p-8 text-white shadow-xl sm:flex-row sm:items-center sm:justify-between">
        <div className="space-y-2">
          <div className="inline-flex items-center gap-2 rounded-full bg-white/20 px-3 py-1 text-xs font-semibold backdrop-blur-sm">
            <Sparkles size={14} />
            Seller Dashboard
          </div>
          <h1 className="text-3xl font-extrabold tracking-tight">Xin chào, {user.displayName ?? "Seller"}</h1>
          <p className="text-emerald-100/90 font-medium">Tổng quan hoạt động kinh doanh của bạn hôm nay.</p>
        </div>
        <div className="flex gap-3">
          <Link href="/seller/products/new">
            <Button variant="secondary" className="bg-white text-emerald-700 shadow-lg hover:bg-emerald-50 border-0">
              <Plus size={18} className="mr-2" />
              Thêm sản phẩm
            </Button>
          </Link>
        </div>
      </header>

      {/* Stats Cards */}
      <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Card className="p-5 flex items-center gap-4 border-emerald-100 bg-emerald-50/50">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-emerald-100 text-emerald-700">
            <Wallet size={24} />
          </div>
          <div>
            <p className="text-sm font-medium text-emerald-800">Doanh thu (50 đơn gần nhất)</p>
            <p className="text-2xl font-bold text-emerald-900">{formatMoney(totalRevenue)}</p>
          </div>
        </Card>
        
        <Card className="p-5 flex items-center gap-4">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-blue-50 text-blue-600">
            <ShoppingBag size={24} />
          </div>
          <div>
            <p className="text-sm font-medium text-zinc-500">Tổng đơn hàng</p>
            <p className="text-2xl font-bold text-zinc-900">{totalOrdersDisplay}</p>
          </div>
        </Card>

        <Card className="p-5 flex items-center gap-4">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-purple-50 text-purple-600">
            <Package2 size={24} />
          </div>
          <div>
            <p className="text-sm font-medium text-zinc-500">Sản phẩm</p>
            <p className="text-2xl font-bold text-zinc-900">{totalProducts}</p>
          </div>
        </Card>

        <Card className="p-5 flex items-center gap-4">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-amber-50 text-amber-600">
            <TrendingUp size={24} />
          </div>
          <div>
            <p className="text-sm font-medium text-zinc-500">Đơn trung bình</p>
            <p className="text-2xl font-bold text-zinc-900">
              {orders.length ? formatMoney(totalRevenue / orders.length) : "0 ₫"}
            </p>
          </div>
        </Card>
      </section>

      <div className="grid gap-8 lg:grid-cols-3">
        {/* Main Chart Section */}
        <section className="lg:col-span-2 space-y-6">
          <Card className="p-6">
            <div className="mb-6 flex items-center justify-between">
              <div>
                <h3 className="font-bold text-zinc-900">Biểu đồ doanh thu</h3>
                <p className="text-sm text-zinc-500">Doanh thu theo ngày gần đây</p>
              </div>
              <div className="rounded-lg bg-zinc-100 p-2 text-zinc-500">
                <Calendar size={20} />
              </div>
            </div>
            <div className="h-[300px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={chartData}>
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
                    contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
                    formatter={(value: number) => [formatMoney(value), "Doanh thu"]}
                  />
                  <Area 
                    type="monotone" 
                    dataKey="value" 
                    stroke="#059669" 
                    strokeWidth={2}
                    fillOpacity={1} 
                    fill="url(#colorRevenue)" 
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </Card>

          <Card className="overflow-hidden">
            <div className="flex items-center justify-between border-b border-zinc-100 bg-zinc-50/50 p-6">
              <h3 className="font-bold text-zinc-900">Đơn hàng gần đây</h3>
              <Link href="/seller/orders" className="text-sm font-medium text-emerald-600 hover:text-emerald-700">
                Xem tất cả
              </Link>
            </div>
            <div className="overflow-x-auto">
              <table className="min-w-full text-left text-sm">
                <thead className="bg-zinc-50 text-zinc-500">
                  <tr>
                    <th className="px-6 py-3 font-semibold">Mã đơn</th>
                    <th className="px-6 py-3 font-semibold">Ngày tạo</th>
                    <th className="px-6 py-3 font-semibold">Tổng tiền</th>
                    <th className="px-6 py-3 font-semibold">Trạng thái</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-zinc-100">
                  {orders.slice(0, 5).map((order) => (
                    <tr key={order.id} className="hover:bg-zinc-50">
                      <td className="px-6 py-4 font-mono text-xs text-zinc-500">{order.id.slice(0, 8)}...</td>
                      <td className="px-6 py-4 text-zinc-600">
                        {new Date(order.createdAt).toLocaleDateString('vi-VN')}
                      </td>
                      <td className="px-6 py-4 font-medium text-zinc-900">
                        {formatMoney(order.total, order.currency)}
                      </td>
                      <td className="px-6 py-4">
                        <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium
                          ${order.status === 'PAID' ? 'bg-green-100 text-green-800' : 
                            order.status === 'CREATED' ? 'bg-blue-100 text-blue-800' : 
                            order.status === 'CANCELLED' ? 'bg-red-100 text-red-800' : 'bg-zinc-100 text-zinc-800'
                          }`}>
                          {order.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                  {!orders.length && (
                    <tr>
                      <td colSpan={4} className="p-6 text-center text-zinc-500">Chưa có đơn hàng nào</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </Card>
        </section>

        {/* Side Panel */}
        <section className="space-y-6">
          {/* Low Stock Alert */}
          <Card className="overflow-hidden border-amber-200 bg-amber-50">
            <div className="border-b border-amber-100 p-4">
              <div className="flex items-center gap-2 font-bold text-amber-900">
                <AlertTriangle size={18} />
                Cảnh báo tồn kho thấp
              </div>
            </div>
            <div className="divide-y divide-amber-100/50 p-0">
              {lowStockProducts.map(p => (
                <div key={p.id} className="flex items-center justify-between p-4 hover:bg-amber-100/50">
                  <div className="overflow-hidden">
                    <p className="truncate font-medium text-amber-900">{p.name}</p>
                    <p className="text-xs text-amber-700">Còn lại: {p.stock}</p>
                  </div>
                  <Button size="icon" variant="ghost" className="h-8 w-8 text-amber-900 hover:bg-amber-200" onClick={() => handleEdit(p.id)}>
                    <ArrowUpRight size={16} />
                  </Button>
                </div>
              ))}
              {!lowStockProducts.length && (
                <div className="p-4 text-center text-sm text-amber-800">Tất cả sản phẩm đều đủ hàng.</div>
              )}
            </div>
          </Card>

           {/* Quick Stats or Tips */}
           <Card className="p-6 bg-gradient-to-br from-indigo-500 to-purple-600 text-white border-0">
              <h3 className="font-bold text-lg mb-2">Mẹo bán hàng</h3>
              <p className="text-indigo-100 text-sm mb-4">
                Cập nhật hình ảnh sản phẩm thường xuyên giúp tăng 30% tỷ lệ chuyển đổi.
              </p>
              <Button variant="secondary" className="w-full bg-white/10 hover:bg-white/20 text-white border-0">
                Xem hướng dẫn
              </Button>
           </Card>
        </section>
      </div>

      {/* Products Table (Existing) */}
      <Card className="overflow-hidden">
        <div className="flex items-center justify-between border-b border-zinc-100 bg-zinc-50/50 p-6">
          <h3 className="font-bold text-zinc-900">Danh sách sản phẩm</h3>
          <Button variant="outline" size="sm" onClick={() => router.push('/seller/products/new')}>
            Quản lý tất cả
          </Button>
        </div>

        <div className="overflow-x-auto">
          <table className="min-w-full text-left text-sm">
            <thead className="bg-zinc-50 text-zinc-500">
              <tr>
                <th className="px-6 py-3 font-semibold">Sản phẩm</th>
                <th className="px-6 py-3 font-semibold">Danh mục</th>
                <th className="px-6 py-3 font-semibold">Giá</th>
                <th className="px-6 py-3 font-semibold">Tồn kho</th>
                <th className="px-6 py-3 font-semibold">Hành động</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {products.slice(0, 10).map((product) => (
                <tr key={product.id} className="hover:bg-zinc-50">
                  <td className="px-6 py-3">
                    <div className="font-semibold text-zinc-900">{product.name}</div>
                    <div className="text-xs text-zinc-500 line-clamp-1">{product.description}</div>
                  </td>
                  <td className="px-6 py-3">{product.category ?? "Chưa phân loại"}</td>
                  <td className="px-6 py-3">{product.price} {product.currency ?? "VND"}</td>
                  <td className="px-6 py-3">
                    <span className={!product.stock ? "text-red-600 font-medium" : ""}>
                      {product.stock ?? "—"}
                    </span>
                  </td>
                  <td className="px-6 py-3">
                    <div className="flex gap-2">
                      <Button
                        size="sm"
                        variant="ghost"
                        onClick={() => handleEdit(product.id)}
                        className="h-8 px-2 text-zinc-600 hover:text-emerald-600"
                      >
                        Sửa
                      </Button>
                      <Button
                        size="sm"
                        variant="ghost"
                        className="h-8 px-2 text-red-600 hover:bg-red-50 hover:text-red-700"
                        onClick={() => handleRemove(product)}
                      >
                        Gỡ
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
              {!products.length && (
                <tr>
                  <td colSpan={5} className="px-6 py-8 text-center text-zinc-500">
                    Chưa có sản phẩm nào.
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
  return (
    <Suspense fallback={
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        Loading dashboard...
      </div>
    }>
      <SellerDashboardContent />
    </Suspense>
  );
}