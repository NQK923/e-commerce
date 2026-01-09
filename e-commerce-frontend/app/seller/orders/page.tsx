'use client';

import React, { Suspense } from "react";
import Link from "next/link";
import { orderApi } from "@/src/api/orderApi";
import { useRequireAuth } from "@/src/hooks/use-require-auth";
import { Badge } from "@/src/components/ui/badge";
import { Button } from "@/src/components/ui/button";
import { Spinner } from "@/src/components/ui/spinner";
import { Order } from "@/src/types/order";
import { MoreHorizontal, ShoppingCart, ChevronLeft, ChevronRight } from "lucide-react";

function OrdersContent() {
  const { user } = useRequireAuth();
  const [orders, setOrders] = React.useState<Order[]>([]);
  const [loading, setLoading] = React.useState(true);
  const [page, setPage] = React.useState(0);
  const [totalPages, setTotalPages] = React.useState(0);

  const loadOrders = React.useCallback(async (pageIndex: number) => {
    if (!user) return;
    setLoading(true);
    try {
      const response = await orderApi.list({ page: pageIndex, size: 10, sellerId: user.id });
      setOrders(response.items || []);
      setTotalPages(response.totalPages || 0);
    } catch (error) {
      console.error("Failed to load orders", error);
    } finally {
      setLoading(false);
    }
  }, [user]);

  React.useEffect(() => {
    if (user) void loadOrders(page);
  }, [loadOrders, page, user]);

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-zinc-900">Quản lý đơn hàng</h1>
          <p className="text-sm text-zinc-500">Xem và quản lý các đơn hàng của khách hàng.</p>
        </div>
      </div>

      <div className="rounded-xl border border-zinc-200 bg-white shadow-sm overflow-hidden">
        {loading ? (
            <div className="flex h-64 items-center justify-center">
                <Spinner />
            </div>
        ) : (
            <>
                <table className="w-full text-left text-sm">
                  <thead className="bg-zinc-50 text-zinc-500 font-medium">
                    <tr>
                      <th className="px-6 py-4">Mã đơn</th>
                      <th className="px-6 py-4">Khách hàng</th>
                      <th className="px-6 py-4">Trạng thái</th>
                      <th className="px-6 py-4">Ngày tạo</th>
                      <th className="px-6 py-4 text-right">Tổng tiền</th>
                      <th className="px-6 py-4">Hành động</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-zinc-100">
                    {orders.map((order) => (
                      <tr key={order.id} className="hover:bg-zinc-50/50">
                        <td className="px-6 py-4 font-mono text-xs text-zinc-500">
                          #{order.id.slice(0, 8)}
                        </td>
                        <td className="px-6 py-4">
                          <div className="font-medium text-zinc-900">{order.userId}</div>
                        </td>
                        <td className="px-6 py-4">
                            <Badge tone={
                              order.status === "PAID" ? "success" :
                              order.status === "SHIPPING" ? "warning" :
                              order.status === "DELIVERED" ? "default" :
                              order.status === "RETURNED" ? "default" :
                              order.status === "CANCELLED" ? "danger" : "warning"
                            }>
                                {order.status}
                            </Badge>
                        </td>
                        <td className="px-6 py-4 text-zinc-500">
                           {new Date(order.createdAt).toLocaleDateString('vi-VN')}
                        </td>
                        <td className="px-6 py-4 text-right font-medium">
                           {order.currency ?? "VND"} {order.total.toLocaleString()}
                        </td>
                        <td className="px-6 py-4">
                          <Link href={`/seller/orders/${order.id}`}>
                            <Button variant="ghost" size="icon">
                                <MoreHorizontal size={16} />
                            </Button>
                          </Link>
                        </td>
                      </tr>
                    ))}
                    {orders.length === 0 && (
                        <tr>
                            <td colSpan={6} className="text-center py-12 flex flex-col items-center gap-2 text-zinc-500">
                                <ShoppingCart size={24} className="opacity-20" />
                                <span>Không có đơn hàng nào.</span>
                            </td>
                        </tr>
                    )}
                  </tbody>
                </table>
                <div className="flex items-center justify-between border-t border-zinc-200 px-6 py-4">
                    <p className="text-sm text-zinc-500">
                        Trang {page + 1} / {totalPages || 1}
                    </p>
                    <div className="flex gap-2">
                        <Button
                            variant="outline"
                            size="sm"
                            disabled={page === 0}
                            onClick={() => setPage((p) => Math.max(0, p - 1))}
                        >
                            <ChevronLeft size={16} /> Trước
                        </Button>
                        <Button
                            variant="outline"
                            size="sm"
                            disabled={page >= totalPages - 1}
                            onClick={() => setPage((p) => p + 1)}
                        >
                            Sau <ChevronRight size={16} />
                        </Button>
                    </div>
                </div>
            </>
        )}
      </div>
    </div>
  );
}

export default function OrdersPage() {
  return (
    <Suspense fallback={<Spinner />}>
      <OrdersContent />
    </Suspense>
  );
}
