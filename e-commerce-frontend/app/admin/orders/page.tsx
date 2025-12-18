'use client';

import React, { Suspense } from "react";
import Link from "next/link";
import { orderApi } from "@/src/api/orderApi";
import { Badge } from "@/src/components/ui/badge";
import { Button } from "@/src/components/ui/button";
import { Spinner } from "@/src/components/ui/spinner";
import { Order } from "@/src/types/order";
import { MoreHorizontal, ShoppingCart, ChevronLeft, ChevronRight } from "lucide-react";

function OrdersContent() {
  const [orders, setOrders] = React.useState<Order[]>([]);
  const [loading, setLoading] = React.useState(true);
  const [page, setPage] = React.useState(0);
  const [totalPages, setTotalPages] = React.useState(0);

  const loadOrders = React.useCallback(async (pageIndex: number) => {
    setLoading(true);
    try {
      const response = await orderApi.list({ page: pageIndex, size: 10 });
      setOrders(response.items || []);
      setTotalPages(response.totalPages || 0);
    } catch (error) {
      console.error("Failed to load orders", error);
    } finally {
      setLoading(false);
    }
  }, []);

  React.useEffect(() => {
    void loadOrders(page);
  }, [loadOrders, page]);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-zinc-900">Orders</h1>
          <p className="text-sm text-zinc-500">Track and manage customer orders.</p>
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
                      <th className="px-6 py-4">Order ID</th>
                      <th className="px-6 py-4">Customer</th>
                      <th className="px-6 py-4">Status</th>
                      <th className="px-6 py-4">Date</th>
                      <th className="px-6 py-4 text-right">Total</th>
                      <th className="px-6 py-4">Actions</th>
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
                           {new Date(order.createdAt).toLocaleDateString()}
                        </td>
                        <td className="px-6 py-4 text-right font-medium">
                           {order.currency ?? "USD"} {order.total.toLocaleString()}
                        </td>
                        <td className="px-6 py-4">
                          <Link href={`/orders/${order.id}`}>
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
                                <span>No orders found.</span>
                            </td>
                        </tr>
                    )}
                  </tbody>
                </table>
                <div className="flex items-center justify-between border-t border-zinc-200 px-6 py-4">
                    <p className="text-sm text-zinc-500">
                        Page {page + 1} of {totalPages || 1}
                    </p>
                    <div className="flex gap-2">
                        <Button
                            variant="outline"
                            size="sm"
                            disabled={page === 0}
                            onClick={() => setPage((p) => Math.max(0, p - 1))}
                        >
                            <ChevronLeft size={16} /> Previous
                        </Button>
                        <Button
                            variant="outline"
                            size="sm"
                            disabled={page >= totalPages - 1}
                            onClick={() => setPage((p) => p + 1)}
                        >
                            Next <ChevronRight size={16} />
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
