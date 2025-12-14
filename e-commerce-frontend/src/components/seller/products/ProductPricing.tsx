import React from "react";
import { useFormContext } from "react-hook-form";
import { Input } from "@/src/components/ui/input";
import { ProductFormValues } from "@/src/schemas/product";

export function ProductPricing() {
  const { register, watch, formState: { errors } } = useFormContext<ProductFormValues>();
  const hasVariants = watch("hasVariants");

  return (
    <div className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm space-y-6">
      <h2 className="text-lg font-bold text-zinc-900">Thông tin bán hàng</h2>

      <div className="grid gap-4 sm:grid-cols-3">
        <div className="grid gap-2">
            <label className="text-sm font-medium text-zinc-700">Giá cơ bản (VND) <span className="text-red-500">*</span></label>
            <Input
              type="number"
              min="0"
              placeholder="0"
              {...register("price")}
              error={errors.price?.message}
            />
        </div>

        <div className="grid gap-2">
             <label className="text-sm font-medium text-zinc-700">Số lượng kho <span className="text-red-500">*</span></label>
             <Input
               type="number"
               min="0"
               disabled={hasVariants}
               placeholder={hasVariants ? "Theo phân loại" : "0"}
               {...register("quantity")}
               error={errors.quantity?.message}
             />
        </div>

        <div className="grid gap-2">
            <label className="text-sm font-medium text-zinc-700">Đơn vị tiền tệ</label>
            <Input
              {...register("currency")}
              disabled
            />
        </div>
      </div>
    </div>
  );
}
