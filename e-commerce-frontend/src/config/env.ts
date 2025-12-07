export type AppConfig = {
  apiBaseUrl: string;
  frontendBaseUrl: string;
  supabaseUrl?: string;
  supabaseAnonKey?: string;
  supabaseProductBucket?: string;
  supabaseSellerBucket?: string;
};

const apiBaseUrl =
  process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/+$/, "") ?? "http://localhost:8080";
const frontendBaseUrl =
  process.env.NEXT_PUBLIC_FRONTEND_BASE_URL?.replace(/\/+$/, "") ?? "http://localhost:3000";
const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL;
const supabaseAnonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY;
const supabaseProductBucket = process.env.NEXT_PUBLIC_SUPABASE_PRODUCT_BUCKET || "product-images";
const supabaseSellerBucket = process.env.NEXT_PUBLIC_SUPABASE_SELLER_BUCKET || "seller-assets";

export const config: AppConfig = {
  apiBaseUrl,
  frontendBaseUrl,
  supabaseUrl,
  supabaseAnonKey,
  supabaseProductBucket,
  supabaseSellerBucket,
};

export const requireConfig = (): AppConfig => {
  if (!config.apiBaseUrl) {
    throw new Error("NEXT_PUBLIC_API_BASE_URL is not defined");
  }
  return config;
};
