'use client';

import React from "react";
import { ShieldCheck, Users, Package2, Store, CheckCircle2, Sparkles, ArrowUpRight, Clock4 } from "lucide-react";
import { productApi } from "@/src/api/productApi";
import { adminApi } from "@/src/api/adminApi";
import { sellerApi } from "@/src/api/sellerApi";
import { Badge } from "@/src/components/ui/badge";
import { Button } from "@/src/components/ui/button";
import { Spinner } from "@/src/components/ui/spinner";
import { useToast } from "@/src/components/ui/toast-provider";
import { ApiError } from "@/src/lib/api-client";
import { Product } from "@/src/types/product";
import { User } from "@/src/types/auth";
import { SellerApplication } from "@/src/types/seller";

type AdminUser = User & {
  status?: "active" | "blocked" | "pending";
  lastActive?: string;
  orders?: number;
};

export default function AdminPage() {
  const { addToast } = useToast();
  const [users, setUsers] = React.useState<AdminUser[]>([]);
  const [products, setProducts] = React.useState<Product[]>([]);
  const [loadingProducts, setLoadingProducts] = React.useState(false);
  const [loadingUsers, setLoadingUsers] = React.useState(false);
  const [sellerRequests, setSellerRequests] = React.useState<SellerApplication[]>([]);
  const [loadingSellerRequests, setLoadingSellerRequests] = React.useState(false);

  React.useEffect(() => {
    const loadUsers = async () => {
      setLoadingUsers(true);
      try {
        const data = await adminApi.users();
        setUsers(
          data.map((user) => ({
            ...user,
            status: "active",
            lastActive: "recently",
            orders: 0,
          })),
        );
      } catch {
        setUsers([]);
      } finally {
        setLoadingUsers(false);
      }
    };

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
    const loadSellerRequests = async () => {
      setLoadingSellerRequests(true);
      try {
        const data = await sellerApi.listApplications();
        setSellerRequests(data ?? []);
      } catch {
        setSellerRequests([]);
      } finally {
        setLoadingSellerRequests(false);
      }
    };
    void loadUsers();
    void loadProducts();
    void loadSellerRequests();
  }, []);

  const toggleUserStatus = (id: string, status: AdminUser["status"]) => {
    setUsers((prev) => prev.map((u) => (u.id === id ? { ...u, status } : u)));
  };

  const reviewApplication = async (id: string, approve: boolean) => {
    try {
      const updated = approve ? await sellerApi.approve(id) : await sellerApi.reject(id);
      setSellerRequests((prev) => prev.map((req) => (req.id === id ? updated : req)));
      addToast(approve ? "Approved seller request" : "Rejected seller request", "success");
    } catch (error) {
      const message = error instanceof ApiError ? error.message : "Unable to update request";
      addToast(message, "error");
    }
  };

  const pendingSellerRequests = sellerRequests.filter((req) => req.status === "PENDING").length;
  const approvedSellerRequests = sellerRequests.filter((req) => req.status === "APPROVED").length;
  const formatDate = (value?: string) => (value ? new Date(value).toLocaleString() : "N/A");

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
              Quản lý người dùng và sản phẩm với dữ liệu thực từ hệ thống.
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
            { icon: Users, label: "Users", value: users.length, tone: "bg-white/10" },
            { icon: Package2, label: "Products", value: products.length || 0, tone: "bg-white/10" },
            { icon: Store, label: "Seller requests", value: pendingSellerRequests, tone: "bg-white/10" },
            { icon: CheckCircle2, label: "Approved sellers", value: approvedSellerRequests, tone: "bg-white/10" },
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
        {loadingUsers ? (
          <div className="mt-6 flex h-32 items-center justify-center gap-3 text-sm text-zinc-500">
            <Spinner />
            Đang tải người dùng...
          </div>
        ) : (
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
                      <div className="font-semibold text-zinc-900">{user.displayName || user.email}</div>
                      <div className="text-xs text-zinc-500">{user.email}</div>
                    </td>
                    <td className="px-4 py-3">
                      <Badge tone={user.roles.includes("ADMIN") ? "success" : user.roles.includes("SELLER") ? "warning" : "default"}>
                        {user.roles[0] ?? "CUSTOMER"}
                      </Badge>
                    </td>
                    <td className="px-4 py-3">
                      <Badge
                        tone={user.status === "active" ? "success" : user.status === "blocked" ? "danger" : "warning"}
                        className="capitalize"
                      >
                        {user.status ?? "active"}
                      </Badge>
                    </td>
                    <td className="px-4 py-3">{user.orders ?? "—"}</td>
                    <td className="px-4 py-3 text-zinc-500">{user.lastActive ?? "—"}</td>
                    <td className="px-4 py-3">
                      <div className="flex flex-wrap gap-2">
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
                {!users.length && (
                  <tr>
                    <td colSpan={6} className="px-4 py-6 text-center text-sm text-zinc-500">
                      Chưa có dữ liệu người dùng.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
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
                  <Badge tone="success">
                    {product.currency ?? "VND"} {product.price}
                  </Badge>
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
            <p className="text-sm font-semibold text-emerald-700">Yeu cau mo gian hang</p>
            <h2 className="text-2xl font-bold text-zinc-900">Seller Requests</h2>
          </div>
          <Badge tone="warning" className="flex items-center gap-1">
            <Clock4 size={14} />
            {pendingSellerRequests} pending
          </Badge>
        </div>
        {loadingSellerRequests ? (
          <div className="mt-4 flex h-24 items-center justify-center gap-2 text-sm text-zinc-600">
            <Spinner />
            Loading seller requests...
          </div>
        ) : sellerRequests.length ? (
          <div className="mt-4 overflow-x-auto rounded-xl border border-zinc-100">
            <table className="min-w-full text-left text-sm">
              <thead className="bg-zinc-50 text-zinc-500">
                <tr>
                  <th className="px-4 py-3 font-semibold">Store</th>
                  <th className="px-4 py-3 font-semibold">Contact</th>
                  <th className="px-4 py-3 font-semibold">Category</th>
                  <th className="px-4 py-3 font-semibold">Status</th>
                  <th className="px-4 py-3 font-semibold">Created</th>
                  <th className="px-4 py-3 font-semibold">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-zinc-100">
                {sellerRequests.map((req) => (
                  <tr key={req.id} className="hover:bg-zinc-50">
                    <td className="px-4 py-3">
                      <div className="font-semibold text-zinc-900">{req.storeName}</div>
                      <div className="text-xs text-zinc-500">#{req.id.slice(0, 8)}</div>
                    </td>
                    <td className="px-4 py-3">
                      <div className="text-sm text-zinc-800">{req.contactEmail}</div>
                      <div className="text-xs text-zinc-500">{req.phone}</div>
                    </td>
                    <td className="px-4 py-3">{req.category || "-"}</td>
                    <td className="px-4 py-3">
                      <Badge
                        tone={req.status === "APPROVED" ? "success" : req.status === "REJECTED" ? "danger" : "warning"}
                        className="uppercase"
                      >
                        {req.status}
                      </Badge>
                    </td>
                    <td className="px-4 py-3 text-zinc-500">{formatDate(req.createdAt)}</td>
                    <td className="px-4 py-3">
                      <div className="flex gap-2">
                        <Button
                          size="sm"
                          variant="primary"
                          disabled={req.status !== "PENDING"}
                          onClick={() => reviewApplication(req.id, true)}
                        >
                          Approve
                        </Button>
                        <Button
                          size="sm"
                          variant="ghost"
                          className="text-red-600 hover:text-red-700"
                          disabled={req.status !== "PENDING"}
                          onClick={() => reviewApplication(req.id, false)}
                        >
                          Reject
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="mt-4 rounded-xl border border-dashed border-zinc-200 p-6 text-sm text-zinc-600">
            No seller requests yet.
          </div>
        )}
      </section>
    </div>
  );
}
