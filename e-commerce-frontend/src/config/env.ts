export type AppConfig = {
  apiBaseUrl: string;
  frontendBaseUrl: string;
  supabaseUrl?: string;
  supabaseAnonKey?: string;
};

const apiBaseUrl =
  process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/+$/, "") ?? "http://localhost:8080";
const frontendBaseUrl =
  process.env.NEXT_PUBLIC_FRONTEND_BASE_URL?.replace(/\/+$/, "") ?? "http://localhost:3000";
const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL;
const supabaseAnonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY;

export const config: AppConfig = {
  apiBaseUrl,
  frontendBaseUrl,
  supabaseUrl,
  supabaseAnonKey,
};

export const requireConfig = (): AppConfig => {
  if (!config.apiBaseUrl) {
    throw new Error("NEXT_PUBLIC_API_BASE_URL is not defined");
  }
  return config;
};
