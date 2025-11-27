export type AppConfig = {
  apiBaseUrl: string;
  frontendBaseUrl: string;
};

const apiBaseUrl =
  process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/+$/, "") ?? "http://localhost:8080";
const frontendBaseUrl =
  process.env.NEXT_PUBLIC_FRONTEND_BASE_URL?.replace(/\/+$/, "") ?? "http://localhost:3000";

export const config: AppConfig = {
  apiBaseUrl,
  frontendBaseUrl,
};

export const requireConfig = (): AppConfig => {
  if (!config.apiBaseUrl) {
    throw new Error("NEXT_PUBLIC_API_BASE_URL is not defined");
  }
  return config;
};
