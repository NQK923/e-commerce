'use client';

import React, { Suspense } from "react";
import Link from "next/link";
import Image from "next/image";
import {
  DollarSign,
  Users,
  ShieldCheck,
  TrendingUp,
  Store,
  Package2,
  CheckCircle2,
  Clock4,
} from "lucide-react";
import { productApi } from "@/src/api/productApi";
import { adminApi } from "@/src/api/adminApi";
import { sellerApi } from "@/src/api/sellerApi";
import { orderApi } from "@/src/api/orderApi";
import { Badge } from "@/src/components/ui/badge";
import { Button } from "@/src/components/ui/button";
import { Spinner } from "@/src/components/ui/spinner";
import { useToast } from "@/src/components/ui/toast-provider";
import { useRequireAuth } from "@/src/hooks/use-require-auth";
import { ApiError } from "@/src/lib/api-client";
import { Product } from "@/src/types/product";
import { User } from "@/src/types/auth";
import { SellerApplication } from "@/src/types/seller";

type AdminUser = User & {
  status?: "active" | "blocked" | "pending";
  lastActive?: string;
  orders?: number;
};

function AdminContent() {
  const { addToast } = useToast();
  const { user, isAuthenticated, initializing } = useRequireAuth("/login");
  const isAdmin = user?.roles?.includes("ADMIN");
  const [users, setUsers] = React.useState<AdminUser[]>([]);
  const [products, setProducts] = React.useState<Product[]>([]);
  const [loadingProducts, setLoadingProducts] = React.useState(false);
  const [sellerRequests, setSellerRequests] = React.useState<SellerApplication[]>([]);
  const [loadingSellerRequests, setLoadingSellerRequests] = React.useState(false);
  const [selectedRequest, setSelectedRequest] = React.useState<SellerApplication | null>(null);
  const [totalRevenue, setTotalRevenue] = React.useState(0);
  const [revenueGrowth, setRevenueGrowth] = React.useState(0);
  const [dashboardError, setDashboardError] = React.useState<string | null>(null);

  React.useEffect(() => {
    if (initializing || !isAuthenticated || !isAdmin) return;
    
    const loadData = async () => {
      setLoadingProducts(true);
      setLoadingSellerRequests(true);
      setDashboardError(null);
      
      try {
        const [usersData, productsRes, requestsData, ordersRes] = await Promise.all([
            adminApi.users().catch(() => []),
            productApi.list({ page: 0, size: 5, includeOutOfStock: true }).catch(() => ({ items: [] })),
            sellerApi.listApplications().catch(() => []),
            orderApi.list({ page: 0, size: 100 }).catch(() => ({ items: [], total: 0 }))
        ]);

        setUsers(usersData.map(u => ({ ...u, status: "active", lastActive: "recently", orders: 0 })));
        setProducts(productsRes.items ?? []);
        setSellerRequests(requestsData ?? []);
        
        const orders = ordersRes.items || [];
        const revenue = orders.reduce((acc, order) => acc + (order.total || 0), 0);
        setTotalRevenue(revenue);

        // Calculate Growth (Current Month vs Last Month)
        const now = new Date();
        const currentMonth = now.getMonth();
        const currentYear = now.getFullYear();
        
        const lastMonthDate = new Date(now.getFullYear(), now.getMonth() - 1, 1);
        const lastMonth = lastMonthDate.getMonth();
        const lastMonthYear = lastMonthDate.getFullYear();

        const currentMonthRevenue = orders
            .filter(o => {
                const d = new Date(o.createdAt);
                return d.getMonth() === currentMonth && d.getFullYear() === currentYear;
            })
            .reduce((acc, o) => acc + (o.total || 0), 0);

        const lastMonthRevenue = orders
            .filter(o => {
                const d = new Date(o.createdAt);
                return d.getMonth() === lastMonth && d.getFullYear() === lastMonthYear;
            })
            .reduce((acc, o) => acc + (o.total || 0), 0);

        if (lastMonthRevenue === 0) {
            setRevenueGrowth(currentMonthRevenue > 0 ? 100 : 0);
        } else {
            setRevenueGrowth(((currentMonthRevenue - lastMonthRevenue) / lastMonthRevenue) * 100);
        }

      } catch (error) {
        console.error("Failed to load dashboard data", error);
        setDashboardError("Không thể tải dữ liệu tổng quan. Vui lòng thử lại.");
        addToast("Không thể tải dữ liệu dashboard", "error");
      } finally {
        setLoadingProducts(false);
        setLoadingSellerRequests(false);
      }
    };

    void loadData();
  }, [initializing, isAuthenticated, isAdmin]);
  const reviewApplication = async (id: string, approve: boolean) => {
    try {
      const updated = approve ? await sellerApi.approve(id) : await sellerApi.reject(id);
      setSellerRequests((prev) => prev.map((req) => (req.id === id ? updated : req)));
      setSelectedRequest((prev) => (prev && prev.id === id ? updated : prev));
      addToast(approve ? "Approved seller request" : "Rejected seller request", "success");
    } catch (error) {
      const message = error instanceof ApiError ? error.message : "Unable to update request";
      addToast(message, "error");
    }
  };

  if (initializing || !isAuthenticated) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        Authenticating...
      </div>
    );
  }

  if (!isAdmin) {
    return (
      <div className="mx-auto flex min-h-[60vh] max-w-2xl flex-col items-center justify-center gap-3 px-4 text-center">
        <div className="h-12 w-12 bg-red-100 text-red-600 rounded-full flex items-center justify-center">
            <ShieldCheck size={24} />
        </div>
        <p className="text-lg font-semibold text-zinc-900">Access Denied</p>
        <p className="text-sm text-zinc-600">You do not have permission to view this page.</p>
      </div>
    );
  }

  const pendingSellerRequests = sellerRequests.filter((req) => req.status === "PENDING").length;
  const formatDate = (value?: string) => (value ? new Date(value).toLocaleString() : "N/A");

  return (
    <div className="space-y-8">
      {dashboardError ? (
        <div className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700 flex items-center justify-between">
          <span>{dashboardError}</span>
          <Button size="sm" variant="outline" onClick={() => window.location.reload()}>
            Thử lại
          </Button>
        </div>
      ) : null}

      <div>
        <h1 className="text-2xl font-bold text-zinc-900">Dashboard Overview</h1>
        <p className="text-sm text-zinc-500">Welcome back, here&apos;s what&apos;s happening with your store today.</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-4 rounded-xl border border-zinc-200 shadow-sm">
            <div className="flex items-center justify-between mb-4">
                <div className="p-2 bg-emerald-100 text-emerald-600 rounded-lg">
                    <DollarSign size={20} />
                </div>
                <span className={`text-xs font-medium px-2 py-1 rounded-full flex items-center gap-1 ${revenueGrowth >= 0 ? 'text-emerald-600 bg-emerald-50' : 'text-red-600 bg-red-50'}`}>
                    <TrendingUp size={12} className={revenueGrowth < 0 ? "rotate-180" : ""} /> 
                    {revenueGrowth >= 0 ? "+" : ""}{revenueGrowth.toFixed(1)}%
                </span>
            </div>
            <p className="text-zinc-500 text-sm">Total Revenue</p>
            <h3 className="text-2xl font-bold text-zinc-900">{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(totalRevenue)}</h3>
        </div>

        <div className="bg-white p-4 rounded-xl border border-zinc-200 shadow-sm">
            <div className="flex items-center justify-between mb-4">
                <div className="p-2 bg-blue-100 text-blue-600 rounded-lg">
                    <Users size={20} />
                </div>
                <span className="text-xs font-medium text-zinc-500">Total</span>
            </div>
            <p className="text-zinc-500 text-sm">Active Users</p>
            <h3 className="text-2xl font-bold text-zinc-900">{users.length}</h3>
        </div>

        <div className="bg-white p-4 rounded-xl border border-zinc-200 shadow-sm">
            <div className="flex items-center justify-between mb-4">
                <div className="p-2 bg-amber-100 text-amber-600 rounded-lg">
                    <Store size={20} />
                </div>
                 {pendingSellerRequests > 0 && (
                    <span className="text-xs font-medium text-amber-600 bg-amber-50 px-2 py-1 rounded-full">
                        {pendingSellerRequests} Pending
                    </span>
                 )}
            </div>
            <p className="text-zinc-500 text-sm">Seller Requests</p>
            <h3 className="text-2xl font-bold text-zinc-900">{sellerRequests.length}</h3>
        </div>

        <div className="bg-white p-4 rounded-xl border border-zinc-200 shadow-sm">
            <div className="flex items-center justify-between mb-4">
                <div className="p-2 bg-purple-100 text-purple-600 rounded-lg">
                    <Package2 size={20} />
                </div>
            </div>
            <p className="text-zinc-500 text-sm">Products</p>
            <h3 className="text-2xl font-bold text-zinc-900">{products.length}</h3>
        </div>
      </div>

      <div className="space-y-8">
            
            {/* Seller Requests Table (Priority) */}
            <section className="bg-white rounded-xl border border-zinc-200 shadow-sm overflow-hidden">
                <div className="p-6 border-b border-zinc-100 flex items-center justify-between">
                    <div>
                        <h2 className="font-bold text-zinc-900">Seller Applications</h2>
                        <p className="text-sm text-zinc-500">Review and approve new store requests.</p>
                    </div>
                    <Link href="/admin/sellers">
                      <Button variant="outline" size="sm">View All</Button>
                    </Link>
                </div>
                
                {loadingSellerRequests ? (
                    <div className="p-8 text-center text-zinc-500">Loading...</div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm text-left">
                            <thead className="bg-zinc-50 text-zinc-500 font-medium border-b border-zinc-100">
                                <tr>
                                    <th className="px-6 py-3">Store</th>
                                    <th className="px-6 py-3">Category</th>
                                    <th className="px-6 py-3">Status</th>
                                    <th className="px-6 py-3">Date</th>
                                    <th className="px-6 py-3 text-right">Action</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-zinc-100">
                                {sellerRequests.slice(0, 5).map((req) => (
                                    <tr key={req.id} className="hover:bg-zinc-50/50 transition-colors">
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-3">
                                                <div className="h-8 w-8 rounded-full bg-zinc-100 flex items-center justify-center overflow-hidden relative">
                                                    {req.avatarUrl ? <Image src={req.avatarUrl} alt="" fill className="object-cover"/> : <Store size={14} className="text-zinc-400"/>}
                                                </div>
                                                <div>
                                                    <p className="font-medium text-zinc-900">{req.storeName}</p>
                                                    <p className="text-xs text-zinc-500">{req.contactEmail}</p>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 text-zinc-600">{req.category || "General"}</td>
                                        <td className="px-6 py-4">
                                            <Badge tone={req.status === "APPROVED" ? "success" : req.status === "PENDING" ? "warning" : "danger"} className="text-xs capitalize">
                                                {req.status.toLowerCase()}
                                            </Badge>
                                        </td>
                                        <td className="px-6 py-4 text-zinc-500 text-xs">{req.createdAt ? new Date(req.createdAt).toLocaleDateString() : 'N/A'}</td>
                                        <td className="px-6 py-4 text-right">
                                            <div className="flex justify-end gap-2">
                                                {req.status === "PENDING" && (
                                                    <>
                                                        <button 
                                                            onClick={() => reviewApplication(req.id, true)}
                                                            className="p-1.5 hover:bg-emerald-50 text-emerald-600 rounded transition-colors" title="Approve">
                                                            <CheckCircle2 size={16} />
                                                        </button>
                                                        <button 
                                                            onClick={() => reviewApplication(req.id, false)}
                                                            className="p-1.5 hover:bg-red-50 text-red-600 rounded transition-colors" title="Reject">
                                                            <Clock4 size={16} className="rotate-45" />
                                                        </button>
                                                    </>
                                                )}
                                                <Button variant="ghost" size="sm" className="h-7 text-xs" onClick={() => setSelectedRequest(req)}>Details</Button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                                {sellerRequests.length === 0 && (
                                    <tr>
                                        <td colSpan={5} className="px-6 py-8 text-center text-zinc-500">No pending requests</td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                )}
            </section>

            {/* Recent Products */}
             <section className="bg-white rounded-xl border border-zinc-200 shadow-sm overflow-hidden">
                <div className="p-6 border-b border-zinc-100 flex items-center justify-between">
                    <div>
                        <h2 className="font-bold text-zinc-900">Recent Products</h2>
                        <p className="text-sm text-zinc-500">Newest additions to the catalog.</p>
                    </div>
                    <Link href="/admin/products">
                      <Button variant="outline" size="sm">Manage Products</Button>
                    </Link>
                </div>
                 {loadingProducts ? (
                    <div className="p-8 text-center text-zinc-500">Loading...</div>
                ) : (
                    <div className="p-4 grid grid-cols-1 sm:grid-cols-2 gap-4">
                        {products.slice(0, 4).map((product) => (
                            <div key={product.id} className="flex items-center gap-4 p-3 rounded-lg border border-zinc-100 hover:border-zinc-200 transition-colors">
                                <div className="h-12 w-12 bg-zinc-100 rounded-lg flex items-center justify-center">
                                    <Package2 size={20} className="text-zinc-400" />
                                </div>
                                <div className="flex-1 min-w-0">
                                    <p className="font-medium text-zinc-900 truncate">{product.name}</p>
                                    <p className="text-xs text-zinc-500">{product.category}</p>
                                </div>
                                <div className="text-right">
                                    <p className="font-semibold text-emerald-600">{product.price} {product.currency}</p>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
             </section>
      </div>
      
      {/* Selected Request Modal/Panel could go here, simplified for now */}
       {selectedRequest && (
          <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4" onClick={(e) => {
              if(e.target === e.currentTarget) setSelectedRequest(null);
          }}>
            <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl animate-in fade-in zoom-in duration-200">
                <div className="flex justify-between items-start mb-4">
                    <div>
                        <h3 className="text-xl font-bold">{selectedRequest.storeName}</h3>
                        <p className="text-sm text-zinc-500">Application Details</p>
                    </div>
                    <button onClick={() => setSelectedRequest(null)} className="text-zinc-400 hover:text-zinc-600">
                        <Clock4 size={20} className="rotate-45" />
                    </button>
                </div>
                
                <div className="space-y-4">
                    <div className="bg-zinc-50 p-4 rounded-lg border border-zinc-100">
                        <div className="grid grid-cols-2 gap-4 text-sm">
                            <div>
                                <p className="text-zinc-500">Contact Email</p>
                                <p className="font-medium text-zinc-900">{selectedRequest.contactEmail}</p>
                            </div>
                             <div>
                                <p className="text-zinc-500">Phone</p>
                                <p className="font-medium text-zinc-900">{selectedRequest.phone}</p>
                            </div>
                            <div>
                                <p className="text-zinc-500">Category</p>
                                <p className="font-medium text-zinc-900">{selectedRequest.category}</p>
                            </div>
                            <div>
                                <p className="text-zinc-500">Date</p>
                                <p className="font-medium text-zinc-900">{formatDate(selectedRequest.createdAt)}</p>
                            </div>
                        </div>
                    </div>
                    
                    <div>
                        <p className="text-sm font-medium mb-2 text-zinc-900">Description</p>
                        <p className="text-sm text-zinc-600 bg-zinc-50 p-3 rounded-lg border border-zinc-100">{selectedRequest.description || "No description provided."}</p>
                    </div>
                </div>

                <div className="mt-6 flex justify-end gap-3">
                    <Button variant="outline" onClick={() => setSelectedRequest(null)}>Close</Button>
                    {selectedRequest.status === 'PENDING' && (
                        <>
                            <Button 
                                className="bg-red-600 hover:bg-red-700 text-white"
                                onClick={() => {
                                    reviewApplication(selectedRequest.id, false);
                                    setSelectedRequest(null);
                                }}
                            >Reject</Button>
                            <Button 
                                className="bg-emerald-600 hover:bg-emerald-700 text-white"
                                onClick={() => {
                                    reviewApplication(selectedRequest.id, true);
                                    setSelectedRequest(null);
                                }}
                            >Approve Application</Button>
                        </>
                    )}
                </div>
            </div>
          </div>
       )}
    </div>
  );
}

export default function AdminPage() {
  return (
    <Suspense fallback={
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        Loading dashboard...
      </div>
    }>
      <AdminContent />
    </Suspense>
  );
}
