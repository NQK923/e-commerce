'use client';

import React from "react";
import { ShieldCheck, Users, Package2, Store, CheckCircle2, XCircle, Clock4, Sparkles, ArrowUpRight } from "lucide-react";
import { productApi } from "@/src/api/productApi";
import { Badge } from "@/src/components/ui/badge";
import { Button } from "@/src/components/ui/button";
import { Spinner } from "@/src/components/ui/spinner";
import { Product } from "@/src/types/product";

type AdminUser = {
  id: string;
  name: string;
  email: string;
  role: "ADMIN" | "CUSTOMER" | "SELLER";
  status: "active" | "blocked" | "pending";
  lastActive: string;
  orders: number;
};

type StoreRequest = {
  id: string;
  user: string;
  category: string;
  status: "pending" | "approved" | "rejected";
  submittedAt: string;
};

const initialUsers: AdminUser[] = [
  { id: "1", name: "NQK Admin", email: "nqk1337@gmail.com", role: "ADMIN", status: "active", lastActive: "2m ago", orders: 42 },
  { id: "2", name: "Jane Seller", email: "jane@sellers.com", role: "SELLER", status: "pending", lastActive: "15m ago", orders: 18 },
  { id: "3", name: "Tuan Customer", email: "tuan@customer.com", role: "CUSTOMER", status: "active", lastActive: "1h ago", orders: 5 },
  { id: "4", name: "Mai Shop", email: "mai@sellers.com", role: "SELLER", status: "blocked", lastActive: "1d ago", orders: 0 },
];

const initialRequests: StoreRequest[] = [
  { id: "req-1", user: "jane@sellers.com", category: "Thời trang", status: "pending", submittedAt: "Hôm nay" },
  { id: "req-2", user: "shop@tech.vn", category: "Điện tử", status: "pending", submittedAt: "Hôm qua" },
  { id: "req-3", user: "coffee@local.vn", category: "Ẩm thực", status: "approved", submittedAt: "2 ngày trước" },
];

export default function AdminPage() {
  const [users, setUsers] = React.useState<AdminUser[]>(initialUsers);
  const [requests, setRequests] = React.useState<StoreRequest[]>(initialRequests);
  const [products, setProducts] = React.useState<Product[]>([]);
  const [loadingProducts, setLoadingProducts] = React.useState(false);

  React.useEffect(() => {
    const loadProducts = async () => {
      setLoadingProducts(true);
      try {
        const response = await productApi.list({ page: 0, size: 8 });
        setProducts(response.items ?? []);
      } catch {
        setProducts([]);
      } finally {
        setLoadingProducts(false);
      }
    };
    void loadProducts();
  }, []);

  const approveRequest = (id: string) => {
    setRequests((prev) => prev.map((r) => (r.id === id ? { ...r, status: "approved" } : r)));
  };

  const rejectRequest = (id: string) => {
    setRequests((prev) => prev.map((r) => (r.id === id ? { ...r, status: "rejected" } : r)));
  };

  const toggleUserStatus = (id: string, status: AdminUser["status"]) => {
    setUsers((prev) => prev.map((u) => (u.id === id ? { ...u, status } : u)));
  };

  const promoteToSeller = (id: string) => {
    setUsers((prev) => prev.map((u) => (u.id === id ? { ...u, role: "SELLER", status: "active" } : u)));
  };

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 space-y-10">
      <section className="rounded-3xl bg-gradient-to-br from-emerald-700 via-emerald-600 to-emerald-800 p-8 text-white shadow-xl">
        <div className="flex flex-col gap-6 sm:flex-row sm:items-center sm:justify-between">
          <div className="space-y-2">
            <div className="inline-flex items-center gap-2 rounded-full bg-white/10 px-3 py-1 text-sm font-semibold">
              <ShieldCheck size={16} />
              Trung tâm quản trị
            </div>
            <h1 className="text-3xl font-extrabold sm:text-4xl">Bảng điều khiển Admin</h1>
            <p className="text-emerald-100 max-w-2xl">
              Quản lý người dùng, sản phẩm và phê duyệt yêu cầu mở gian hàng ở một nơi tập trung.
            </p>
          </div>
          <div className="flex items-center gap-3 rounded-2xl bg-white/10 px-4 py-3 backdrop-blur">
            <Sparkles size={18} className="text-yellow-300" />
            <div>
              <p className="text-sm text-emerald-100">Tài khoản admin</p>
              <p className="font-semibold">nqk1337@gmail.com</p>
            </div>
          </div>
        </div>
        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {[
            { icon: Users, label: "Người dùng", value: users.length, tone: "bg-white/10" },
            { icon: Package2, label: "Sản phẩm", value: products.length || "–", tone: "bg-white/10" },
            { icon: Store, label: "Yêu cầu gian hàng", value: requests.length, tone: "bg-white/10" },
            { icon: CheckCircle2, label: "Duyệt hôm nay", value: requests.filter((r) => r.status === "approved").length, tone: "bg-white/10" },
          ].map((item) => (
            <div key={item.label} className={`flex items-center gap-4 rounded-2xl px-4 py-3 ${item.tone}`}>
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-white/20">
                <item.icon size={22} />
              </div>
              <div>
                <p className="text-sm text-emerald-100">{item.label}</p>
                <p className="text-xl font-bold">{item.value}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <p className="text-sm font-semibold text-emerald-700">Quản lý người dùng</p>
            <h2 className="text-2xl font-bold text-zinc-900">Users</h2>
          </div>
          <Badge tone="warning" className="flex items-center gap-1">
            <Clock4 size={14} />
            {users.filter((u) => u.status === "pending").length} chờ duyệt
          </Badge>
        </div>
        <div className="mt-4 overflow-x-auto rounded-xl border border-zinc-100">
          <table className="min-w-full text-left text-sm">
            <thead className="bg-zinc-50 text-zinc-500">
              <tr>
                <th className="px-4 py-3 font-semibold">User</th>
                <th className="px-4 py-3 font-semibold">Role</th>
                <th className="px-4 py-3 font-semibold">Trạng thái</th>
                <th className="px-4 py-3 font-semibold">Đơn hàng</th>
                <th className="px-4 py-3 font-semibold">Hoạt động</th>
                <th className="px-4 py-3 font-semibold">Hành động</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {users.map((user) => (
                <tr key={user.id} className="hover:bg-zinc-50">
                  <td className="px-4 py-3">
                    <div className="font-semibold text-zinc-900">{user.name}</div>
                    <div className="text-xs text-zinc-500">{user.email}</div>
                  </td>
                  <td className="px-4 py-3">
                    <Badge tone={user.role === "ADMIN" ? "success" : user.role === "SELLER" ? "warning" : "default"}>
                      {user.role}
                    </Badge>
                  </td>
                  <td className="px-4 py-3">
                    <Badge
                      tone={user.status === "active" ? "success" : user.status === "blocked" ? "danger" : "warning"}
                      className="capitalize"
                    >
                      {user.status}
                    </Badge>
                  </td>
                  <td className="px-4 py-3">{user.orders}</td>
                  <td className="px-4 py-3 text-zinc-500">{user.lastActive}</td>
                  <td className="px-4 py-3">
                    <div className="flex flex-wrap gap-2">
                      {user.role !== "SELLER" && (
                        <Button size="sm" variant="outline" onClick={() => promoteToSeller(user.id)}>
                          Nâng lên Seller
                        </Button>
                      )}
                      {user.status !== "blocked" ? (
                        <Button size="sm" variant="ghost" onClick={() => toggleUserStatus(user.id, "blocked")}>
                          Khóa
                        </Button>
                      ) : (
                        <Button size="sm" variant="primary" onClick={() => toggleUserStatus(user.id, "active")}>
                          Mở khóa
                        </Button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm font-semibold text-emerald-700">Quản lý sản phẩm</p>
            <h2 className="text-2xl font-bold text-zinc-900">Products</h2>
          </div>
          <Button variant="secondary" size="sm" className="gap-2">
            Tạo sản phẩm
            <ArrowUpRight size={16} />
          </Button>
        </div>
        {loadingProducts ? (
          <div className="mt-6 flex h-40 items-center justify-center gap-3 text-zinc-500">
            <Spinner />
            Đang tải sản phẩm...
          </div>
        ) : (
          <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {products.map((product) => (
              <div key={product.id} className="flex flex-col gap-3 rounded-xl border border-zinc-100 p-4 shadow-sm">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="text-xs uppercase text-emerald-600">#{product.id.slice(0, 8)}</p>
                    <h3 className="text-lg font-semibold text-zinc-900">{product.name}</h3>
                    <p className="text-sm text-zinc-500 line-clamp-2">{product.description}</p>
                  </div>
                  <Badge tone="success">{product.currency ?? "VND"} {product.price}</Badge>
                </div>
                <div className="flex items-center gap-2 text-xs text-zinc-500">
                  <Package2 size={14} />
                  {product.category ?? "Chưa phân loại"}
                </div>
                <div className="flex gap-2">
                  <Button size="sm" variant="outline" className="flex-1">
                    Chỉnh sửa
                  </Button>
                  <Button size="sm" variant="ghost" className="flex-1 text-red-600 hover:text-red-700">
                    Gỡ xuống
                  </Button>
                </div>
              </div>
            ))}
            {!products.length && (
              <div className="col-span-full flex h-32 items-center justify-center rounded-xl border border-dashed border-zinc-200 text-zinc-500">
                Chưa có sản phẩm để quản lý.
              </div>
            )}
          </div>
        )}
      </section>

      <section className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm font-semibold text-emerald-700">Yêu cầu mở gian hàng</p>
            <h2 className="text-2xl font-bold text-zinc-900">Seller Requests</h2>
          </div>
          <Badge tone="warning" className="flex items-center gap-1">
            <Clock4 size={14} />
            {requests.filter((r) => r.status === "pending").length} pending
          </Badge>
        </div>
        <div className="mt-4 grid gap-3 sm:grid-cols-2">
          {requests.map((req) => (
            <div key={req.id} className="flex flex-col gap-3 rounded-xl border border-zinc-100 p-4 shadow-sm">
              <div className="flex items-center justify-between gap-3">
                <div>
                  <p className="text-sm font-semibold text-zinc-900">{req.user}</p>
                  <p className="text-xs text-zinc-500">Ngành hàng: {req.category}</p>
                  <p className="text-xs text-zinc-400">Gửi: {req.submittedAt}</p>
                </div>
                <Badge tone={req.status === "approved" ? "success" : req.status === "rejected" ? "danger" : "warning"} className="capitalize">
                  {req.status}
                </Badge>
              </div>
              <div className="flex gap-2">
                <Button
                  size="sm"
                  variant="primary"
                  className="flex-1"
                  disabled={req.status === "approved"}
                  onClick={() => approveRequest(req.id)}
                >
                  Duyệt
                </Button>
                <Button
                  size="sm"
                  variant="ghost"
                  className="flex-1 text-red-600 hover:text-red-700"
                  disabled={req.status === "rejected"}
                  onClick={() => rejectRequest(req.id)}
                >
                  Từ chối
                </Button>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
