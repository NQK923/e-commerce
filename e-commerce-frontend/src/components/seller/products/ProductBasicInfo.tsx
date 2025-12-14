import React from "react";
import { useFormContext } from "react-hook-form";
import { Input } from "@/src/components/ui/input";
import { PRODUCT_CATEGORIES } from "@/src/constants/categories";
import { ProductFormValues } from "@/src/schemas/product";

export function ProductBasicInfo() {
  const { register, formState: { errors } } = useFormContext<ProductFormValues>();

  return (
    <div className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm space-y-6">
      <h2 className="text-lg font-bold text-zinc-900">Thông tin cơ bản</h2>
      <div className="grid gap-6">
        <div className="grid gap-2">
          <label className="text-sm font-medium text-zinc-700">
            Tên sản phẩm <span className="text-red-500">*</span>
          </label>
          <Input
            {...register("name")}
            placeholder="VD: Áo thun nam cotton..."
            error={errors.name?.message}
          />
        </div>

        <div className="grid gap-2">
          <label className="text-sm font-medium text-zinc-700">
            Ngành hàng <span className="text-red-500">*</span>
          </label>
          <select
            {...register("categoryId")}
            className="h-10 w-full rounded-lg border border-zinc-200 bg-white px-3 text-sm outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
          >
            {PRODUCT_CATEGORIES.map((cat) => (
              <option key={cat.value} value={cat.value}>
                {cat.label}
              </option>
            ))}
          </select>
          {errors.categoryId && (
            <span className="text-xs text-red-500">{errors.categoryId.message}</span>
          )}
        </div>

        <div className="grid gap-2">
          <label className="text-sm font-medium text-zinc-700">Mô tả sản phẩm</label>
          <textarea
            {...register("description")}
            className="min-h-[150px] rounded-lg border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-800 outline-none transition focus:border-emerald-400 focus:ring-2 focus:ring-emerald-100"
            placeholder="Mô tả chi tiết về sản phẩm..."
          />
        </div>
      </div>
    </div>
  );
}
