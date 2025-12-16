export type ProductImage = {
  id: string;
  url: string;
  altText?: string;
  primary?: boolean;
};

export type Product = {
  id: string;
  name: string;
  description: string;
  shortDescription?: string;
  price: number;
  currency?: string;
  sku?: string;
  stock?: number;
  soldCount?: number;
  rating?: number;
  category?: string;
  sellerId?: string;
  flashSaleStartAt?: string;
  flashSaleEndAt?: string;
  discountPercentage?: number;
  images: ProductImage[];
  variants?: ProductVariantRequest[];
};

export type ProductVariantRequest = {
  sku: string;
  name: string;
  price: number;
  quantity: number;
};

export type UpsertProductRequest = {
  id?: string;
  name: string;
  description?: string;
  price: number;
  currency?: string;
  quantity?: number;
  categoryId?: string;
  sellerId?: string;
  images: Array<{
    id?: string;
    url: string;
    altText?: string;
    primary?: boolean;
    sortOrder?: number;
  }>;
  variants?: ProductVariantRequest[];
};

export type ProductListParams = {
  search?: string;
  category?: string;
  minPrice?: number;
  maxPrice?: number;
  page?: number;
  size?: number;
  sort?: string;
  includeOutOfStock?: boolean;
  sellerId?: string;
};
