export type ProductCategoryOption = {
  value: string;
  label: string;
};

export const PRODUCT_CATEGORIES: ProductCategoryOption[] = [
  { value: "Điện tử", label: "Điện tử" },
  { value: "Thời trang", label: "Thời trang" },
  { value: "Gia dụng", label: "Gia dụng" },
  { value: "Sách", label: "Sách" },
  { value: "Làm đẹp", label: "Làm đẹp" },
  { value: "Sức khỏe", label: "Sức khỏe" },
  { value: "Đồ chơi", label: "Đồ chơi" },
  { value: "Thể thao", label: "Thể thao" },
  { value: "Ô tô - Xe máy", label: "Ô tô - Xe máy" },
  { value: "Bách hóa online", label: "Bách hóa online" },
  { value: "Khác", label: "Khác" },
];

export const DEFAULT_PRODUCT_CATEGORY = PRODUCT_CATEGORIES[0]?.value ?? "";
