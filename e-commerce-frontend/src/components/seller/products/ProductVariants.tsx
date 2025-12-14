import React, { useState } from "react";
import { useFormContext, useFieldArray } from "react-hook-form";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { Trash2, Plus } from "lucide-react";
import { ProductFormValues } from "@/src/schemas/product";

export function ProductVariants() {
  const { register, control, watch, formState: { errors }, trigger } = useFormContext<ProductFormValues>();
  const { fields, append, remove } = useFieldArray({
    control,
    name: "variants",
  });

  const hasVariants = watch("hasVariants");

  // Local state for the "Add Variant" inputs
  const [newVariant, setNewVariant] = useState({
    name: "",
    sku: "",
    price: "",
    quantity: "" // keep as string for input, parse when adding
  });
  
  const [addError, setAddError] = useState<string | null>(null);

  const handleAddVariant = () => {
    if (!newVariant.name || !newVariant.sku || !newVariant.price || !newVariant.quantity) {
      setAddError("Vui lòng nhập đầy đủ thông tin phân loại");
      return;
    }
    
    // Validate quantity is a number
    const qty = parseInt(newVariant.quantity);
    if (isNaN(qty) || qty < 0) {
        setAddError("Số lượng không hợp lệ");
        return;
    }

    append({
      name: newVariant.name,
      sku: newVariant.sku,
      price: newVariant.price,
      quantity: qty,
    });

    setNewVariant({ name: "", sku: "", price: "", quantity: "" });
    setAddError(null);
    trigger("variants"); // Trigger validation for variants array
  };

  return (
    <div className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm space-y-6">
      <div className="flex items-center gap-2 cursor-pointer select-none mb-4">
        <input
          type="checkbox"
          id="hasVariants"
          className="h-4 w-4 rounded border-zinc-300 text-emerald-600 focus:ring-emerald-500"
          {...register("hasVariants")}
        />
        <label htmlFor="hasVariants" className="font-medium text-zinc-900 cursor-pointer">
            Sản phẩm có nhiều phân loại (Màu sắc, Kích cỡ...)
        </label>
      </div>

      {hasVariants && (
        <div className="rounded-lg border border-zinc-100 bg-zinc-50 p-4 space-y-4">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-5 items-end">
            <div className="grid gap-2">
                <label className="text-xs font-medium text-zinc-700">Tên phân loại</label>
                <Input
                  placeholder="VD: Đỏ, Size L"
                  value={newVariant.name}
                  onChange={(e) => setNewVariant({ ...newVariant, name: e.target.value })}
                />
            </div>
             <div className="grid gap-2">
                <label className="text-xs font-medium text-zinc-700">SKU</label>
                <Input
                  placeholder="Mã kho"
                  value={newVariant.sku}
                  onChange={(e) => setNewVariant({ ...newVariant, sku: e.target.value })}
                />
            </div>
             <div className="grid gap-2">
                <label className="text-xs font-medium text-zinc-700">Giá bán</label>
                <Input
                  type="number"
                  placeholder="0"
                  value={newVariant.price}
                  onChange={(e) => setNewVariant({ ...newVariant, price: e.target.value })}
                />
            </div>
             <div className="grid gap-2">
                <label className="text-xs font-medium text-zinc-700">Số lượng</label>
                <Input
                  type="number"
                  placeholder="0"
                  value={newVariant.quantity}
                  onChange={(e) => setNewVariant({ ...newVariant, quantity: e.target.value })}
                />
            </div>
            
            <Button
              type="button"
              onClick={handleAddVariant}
              className="bg-white border border-zinc-300 text-zinc-700 hover:bg-zinc-50 h-10 w-full"
            >
              <Plus size={16} className="mr-1" /> Thêm
            </Button>
          </div>
          
          {addError && <p className="text-xs text-red-500">{addError}</p>}
          {errors.variants?.message && <p className="text-xs text-red-500">{errors.variants.message}</p>}

          {fields.length > 0 && (
            <div className="overflow-hidden rounded-lg border border-zinc-200 bg-white shadow-sm mt-4">
               <div className="bg-zinc-50 px-4 py-3 border-b border-zinc-100">
                   <h3 className="text-sm font-semibold text-zinc-900">Danh sách phân loại</h3>
                   <p className="text-xs text-zinc-500">Bạn có thể chỉnh sửa trực tiếp thông tin bên dưới.</p>
               </div>
              <table className="w-full text-sm text-left">
                <thead className="bg-zinc-50 text-zinc-700">
                  <tr>
                    <th className="px-4 py-3 font-medium">Tên phân loại</th>
                    <th className="px-4 py-3 font-medium">SKU</th>
                    <th className="px-4 py-3 font-medium">Giá</th>
                    <th className="px-4 py-3 font-medium">Số lượng</th>
                    <th className="px-4 py-3 font-medium text-right">Hành động</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-zinc-100">
                  {fields.map((field, idx) => (
                    <tr key={field.id} className="group hover:bg-zinc-50/50">
                      <td className="px-4 py-2">
                         <input 
                            {...register(`variants.${idx}.name`)} 
                            className="w-full rounded border border-transparent bg-transparent px-2 py-1 text-zinc-900 transition-all hover:border-zinc-300 focus:border-emerald-500 focus:bg-white focus:outline-none"
                         />
                      </td>
                      <td className="px-4 py-2">
                         <input 
                            {...register(`variants.${idx}.sku`)} 
                            className="w-full rounded border border-transparent bg-transparent px-2 py-1 text-zinc-500 transition-all hover:border-zinc-300 focus:border-emerald-500 focus:bg-white focus:outline-none focus:text-zinc-900"
                         />
                      </td>
                      <td className="px-4 py-2">
                         <input 
                            {...register(`variants.${idx}.price`)} 
                            className="w-full rounded border border-transparent bg-transparent px-2 py-1 font-medium text-emerald-600 transition-all hover:border-zinc-300 focus:border-emerald-500 focus:bg-white focus:outline-none"
                         />
                      </td>
                      <td className="px-4 py-2">
                         <input 
                            type="number"
                            {...register(`variants.${idx}.quantity`, { valueAsNumber: true })} 
                            className="w-full rounded border border-transparent bg-transparent px-2 py-1 text-zinc-600 transition-all hover:border-zinc-300 focus:border-emerald-500 focus:bg-white focus:outline-none focus:text-zinc-900"
                         />
                      </td>
                      <td className="px-4 py-2 text-right">
                        <button
                          type="button"
                          onClick={() => remove(idx)}
                          className="rounded p-1 text-zinc-400 hover:bg-red-50 hover:text-red-600 transition-colors"
                          title="Xóa phân loại"
                        >
                          <Trash2 size={16} />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  );
}