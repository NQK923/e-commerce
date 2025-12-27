import { apiRequest } from "../lib/api-client";

export type DiscountType = "PERCENTAGE" | "FIXED_AMOUNT";

export type Coupon = {
  id: string;
  code: string;
  sellerId: string;
  applicableProductIds: string[];
  discountType: DiscountType;
  discountValue: number;
  minOrderValue?: { amount: number; currency: string };
  maxDiscountAmount?: { amount: number; currency: string };
  usageLimit: number;
  usedCount: number;
  startAt: string;
  endAt: string;
};

export type CreateCouponRequest = {
  code: string;
  applicableProductIds: string[];
  discountType: DiscountType;
  discountValue: number;
  minOrderAmount?: number;
  currency?: string;
  maxDiscountAmount?: number;
  usageLimit: number;
  startAt: string;
  endAt: string;
};

export const promotionApi = {
  sellerListCoupons: () => apiRequest<Coupon[]>("/api/seller/promotions/coupons"),
  sellerCreateCoupon: (data: CreateCouponRequest) => apiRequest<Coupon>("/api/seller/promotions/coupons", { method: "POST", body: data }),
};
