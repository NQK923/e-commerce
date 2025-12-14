import { z } from "zod";

export const productVariantSchema = z.object({
  sku: z.string().min(1, "SKU là bắt buộc"),
  name: z.string().min(1, "Tên phân loại là bắt buộc"),
  price: z.string().min(1, "Giá là bắt buộc"), // Input as string to handle varying formats, validate as number if needed
  quantity: z.number().min(0, "Số lượng phải lớn hơn hoặc bằng 0"),
});

export const productSchema = z.object({
  name: z.string().min(1, "Tên sản phẩm là bắt buộc").max(200, "Tên sản phẩm quá dài"),
  description: z.string().optional(),
  categoryId: z.string().min(1, "Vui lòng chọn ngành hàng"),
  price: z.string().min(1, "Giá sản phẩm là bắt buộc"),
  currency: z.string().default("VND"),
  quantity: z.string().optional(), // Base quantity if no variants
  images: z.array(z.string()).min(1, "Vui lòng tải lên ít nhất 1 hình ảnh"),
  hasVariants: z.boolean().default(false),
  variants: z.array(productVariantSchema).optional(),
}).refine((data) => {
  if (data.hasVariants) {
    return data.variants && data.variants.length > 0;
  }
  return true;
}, {
  message: "Vui lòng thêm ít nhất 1 phân loại hoặc tắt chế độ phân loại",
  path: ["variants"],
}).refine((data) => {
  if (!data.hasVariants) {
    return !!data.quantity && parseInt(data.quantity) >= 0;
  }
  return true;
}, {
  message: "Vui lòng nhập số lượng kho",
  path: ["quantity"],
});

export type ProductFormValues = z.infer<typeof productSchema>;
export type ProductVariantValues = z.infer<typeof productVariantSchema>;
