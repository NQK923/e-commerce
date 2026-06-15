export type AppConfig = {
  apiBaseUrl: string;
  frontendBaseUrl: string;
  supabaseUrl?: string;
  supabaseAnonKey?: string;
  supabaseProductBucket?: string;
  supabaseSellerBucket?: string;
  supabaseChatBucket?: string;
  uploadFallbackMode: "disabled" | "placeholder";
  oauthDevMode: boolean;
};

const apiBaseUrl =
  process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/+$/, "") ?? "http://localhost:8080";
const frontendBaseUrl =
  process.env.NEXT_PUBLIC_FRONTEND_BASE_URL?.replace(/\/+$/, "") ?? "http://localhost:3000";
const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL;
const supabaseAnonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY;
const supabaseProductBucket = process.env.NEXT_PUBLIC_SUPABASE_PRODUCT_BUCKET || "product-images";
const supabaseSellerBucket = process.env.NEXT_PUBLIC_SUPABASE_SELLER_BUCKET || "seller-assets";
const supabaseChatBucket = process.env.NEXT_PUBLIC_SUPABASE_CHAT_BUCKET || "chat-attachments";
const uploadFallbackMode = process.env.NEXT_PUBLIC_UPLOAD_FALLBACK_MODE === "disabled"
  ? "disabled"
  : process.env.NEXT_PUBLIC_UPLOAD_FALLBACK_MODE === "placeholder" || process.env.NODE_ENV !== "production"
    ? "placeholder"
    : "disabled";
const oauthDevMode = process.env.NEXT_PUBLIC_OAUTH_DEV_MODE === "enabled";

export const config: AppConfig = {
  apiBaseUrl,
  frontendBaseUrl,
  supabaseUrl,
  supabaseAnonKey,
  supabaseProductBucket,
  supabaseSellerBucket,
  supabaseChatBucket,
  uploadFallbackMode,
  oauthDevMode,
};

export const requireConfig = (): AppConfig => {
  if (!config.apiBaseUrl) {
    throw new Error("NEXT_PUBLIC_API_BASE_URL is not defined");
  }
  return config;
};
