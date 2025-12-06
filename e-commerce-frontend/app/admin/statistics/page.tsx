'use client';

import React, { Suspense, useMemo, useState, useEffect } from 'react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  LineChart,
  Line,
  Legend
} from 'recharts';
import { orderApi } from '@/src/api/orderApi';
import { adminApi } from '@/src/api/adminApi';
import { productApi } from '@/src/api/productApi';
import { Spinner } from '@/src/components/ui/spinner';
import { Order } from '@/src/types/order';
import { User } from '@/src/types/auth';
import { Product } from '@/src/types/product';

// Colors for charts
const COLORS = ['#10b981', '#3b82f6', '#f59e0b', '#ef4444'];

export default function StatisticsPage() {
  const [loading, setLoading] = useState(true);
  const [orders, setOrders] = useState<Order[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [products, setProducts] = useState<Product[]>([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [ordersRes, usersRes, productsRes] = await Promise.all([
          orderApi.list(0, 100).catch(() => ({ items: [] })),
          adminApi.users().catch(() => []),
          productApi.list({ size: 100 }).catch(() => ({ items: [] }))
        ]);

        setOrders(ordersRes.items || []);
        setUsers(usersRes);
        setProducts(productsRes.items || []);
      } catch (error) {
        console.error("Error fetching stats data:", error);
      } finally {
        setLoading(false);
      }
    };

    void fetchData();
  }, []);

  const revenueData = useMemo(() => {
    const data: Record<string, number> = {};
    // Show last 7 days or reasonable range. 
    // For demo, grouping all fetched orders by Date (MMM dd)
    const sortedOrders = [...orders].sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
    
    sortedOrders.forEach(order => {
        const date = new Date(order.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
        data[date] = (data[date] || 0) + (order.total || 0);
    });

    return Object.entries(data).map(([name, value]) => ({ name, value }));
  }, [orders]);

  const orderStatusData = useMemo(() => {
    const data: Record<string, number> = {};
    orders.forEach(order => {
        const status = order.status || 'UNKNOWN';
        data[status] = (data[status] || 0) + 1;
    });
    return Object.entries(data).map(([name, value]) => ({ name, value }));
  }, [orders]);

  const topProducts = useMemo(() => {
      // This requires Order Items to have productId. 
      // If we can't get that easily from Order summary, we might skip or use dummy data if necessary
      // But orderApi detail mapping has items. 
      const productCounts: Record<string, number> = {};
      orders.forEach(order => {
          order.items.forEach(item => {
             productCounts[item.productId] = (productCounts[item.productId] || 0) + item.quantity;
          });
      });
      
      // Map back to product names
      return Object.entries(productCounts)
        .map(([id, count]) => {
            const product = products.find(p => p.id === id);
            return { name: product?.name || id, count };
        })
        .sort((a, b) => b.count - a.count)
        .slice(0, 5);
  }, [orders, products]);


  if (loading) {
    return (
      <div className="flex h-[60vh] items-center justify-center">
        <Spinner />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-zinc-900">Statistics</h1>
        <p className="text-sm text-zinc-500">Detailed analysis of your store&#39;s performance.</p>
      </div>

      {/* Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        
        {/* Revenue Chart */}
        <div className="bg-white p-6 rounded-xl border border-zinc-200 shadow-sm lg:col-span-2">
          <h3 className="text-lg font-semibold mb-4">Revenue Trends</h3>
          <div className="h-80 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={revenueData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f3f4f6" />
                <XAxis dataKey="name" stroke="#9ca3af" fontSize={12} tickLine={false} axisLine={false} />
                <YAxis stroke="#9ca3af" fontSize={12} tickLine={false} axisLine={false} tickFormatter={(val: number) => `$${val}`} />
                <Tooltip 
                    contentStyle={{ backgroundColor: '#fff', borderRadius: '8px', border: '1px solid #e5e7eb', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                    cursor={{ fill: '#f9fafb' }}
                />
                <Bar dataKey="value" fill="#10b981" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Order Status Distribution */}
        <div className="bg-white p-6 rounded-xl border border-zinc-200 shadow-sm">
          <h3 className="text-lg font-semibold mb-4">Order Status</h3>
          <div className="h-64 w-full flex items-center justify-center">
             <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                    <Pie
                        data={orderStatusData}
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={80}
                        paddingAngle={5}
                        dataKey="value"
                    >
                        {orderStatusData.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                    </Pie>
                    <Tooltip />
                    <Legend verticalAlign="bottom" height={36}/>
                </PieChart>
             </ResponsiveContainer>
          </div>
        </div>

        {/* Top Products Table */}
         <div className="bg-white p-6 rounded-xl border border-zinc-200 shadow-sm">
            <h3 className="text-lg font-semibold mb-4">Top Selling Products</h3>
            <div className="overflow-x-auto">
                <table className="w-full text-sm text-left">
                    <thead className="bg-zinc-50 text-zinc-500 font-medium">
                        <tr>
                            <th className="px-4 py-2">Product</th>
                            <th className="px-4 py-2 text-right">Sold</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-zinc-100">
                        {topProducts.map((item, idx) => (
                            <tr key={idx}>
                                <td className="px-4 py-3 font-medium text-zinc-900 truncate max-w-[200px]">{item.name}</td>
                                <td className="px-4 py-3 text-right text-emerald-600 font-semibold">{item.count}</td>
                            </tr>
                        ))}
                        {topProducts.length === 0 && (
                            <tr><td colSpan={2} className="text-center py-4 text-zinc-400">No data available</td></tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>

      </div>
    </div>
  );
}
