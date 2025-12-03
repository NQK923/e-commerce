'use client';

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { productApi } from "@/src/api/productApi";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { Spinner } from "@/src/components/ui/spinner";
import { useToast } from "@/src/components/ui/toast-provider";
import { useRequireAuth } from "@/src/hooks/use-require-auth";

export default function NewProductPage() {
  const router = useRouter();
  const { addToast } = useToast();
  const { user, initializing } = useRequireAuth("/login");
  const [form, setForm] = useState({
    name: "",
    description: "",
    price: "",
    currency: "VND",
    categoryId: "",
    imageUrl: "",
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.name || !form.price || !form.imageUrl) {
      addToast("Vui lòng nhập tên, giá và hình ảnh sản phẩm", "error");
      return;
    }
    setLoading(true);
    try {
      await productApi.create({
        name: form.name,
        description: form.description,
        price: parseFloat(form.price),
        currency: form.currency || "VND",
        categoryId: form.categoryId || undefined,
        images: [
          {
            url: form.imageUrl,
            primary: true,
          },
        ],
      });
      addToast("Đã tạo sản phẩm", "success");
      router.replace("/seller/dashboard");
    } catch (error) {
      const message = error instanceof Error ? error.message : "Không thể tạo sản phẩm";
      addToast(message, "error");
    } finally {
      setLoading(false);
    }
  };

  if (initializing || !user) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        Loading...
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-10 space-y-6">
      <div>
        <p className="text-sm font-semibold text-emerald-700">Sản phẩm</p>
        <h1 className="text-3xl font-bold text-zinc-900">Thêm sản phẩm mới</h1>
        <p className="text-sm text-zinc-600">Nhập thông tin cơ bản để đăng sản phẩm lên gian hàng.</p>
      </div>

      <form className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm space-y-4" onSubmit={handleSubmit}>
        <Input
          label="Tên sản phẩm"
          required
          value={form.name}
          onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))}
        />
        <div className="flex flex-col gap-2">
          <label className="text-sm font-medium text-zinc-700">Mô tả</label>
          <textarea
            className="min-h-[120px] rounded-lg border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-800 outline-none transition focus:border-emerald-400 focus:ring-2 focus:ring-emerald-100"
            value={form.description}
            onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))}
            placeholder="Mô tả ngắn về sản phẩm, chất liệu, điểm nổi bật..."
          />
        </div>
        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            label="Giá"
            type="number"
            min="0"
            step="0.01"
            required
            value={form.price}
            onChange={(e) => setForm((prev) => ({ ...prev, price: e.target.value }))}
          />
          <Input
            label="Tiền tệ"
            value={form.currency}
            onChange={(e) => setForm((prev) => ({ ...prev, currency: e.target.value }))}
          />
        </div>
        <Input
          label="Danh mục (tùy chọn)"
          value={form.categoryId}
          onChange={(e) => setForm((prev) => ({ ...prev, categoryId: e.target.value }))}
        />
        <Input
          label="Ảnh sản phẩm (URL)"
          required
          value={form.imageUrl}
          onChange={(e) => setForm((prev) => ({ ...prev, imageUrl: e.target.value }))}
          placeholder="https://..."
        />

        <div className="flex gap-3">
          <Button type="submit" disabled={loading} className="bg-emerald-600 hover:bg-emerald-700">
            {loading ? "Đang tạo..." : "Tạo sản phẩm"}
          </Button>
          <Button type="button" variant="ghost" onClick={() => router.back()}>
            Hủy
          </Button>
        </div>
      </form>
    </div>
  );
}
