import { apiRequest } from "../lib/api-client";
import {
  AuthResponse,
  AuthTokens,
  LoginRequest,
  RegisterRequest,
  User,
} from "../types/auth";

export const authApi = {
  register: (payload: RegisterRequest) =>
    apiRequest<AuthResponse>("/api/auth/register", {
      method: "POST",
      body: payload,
    }),

  login: (payload: LoginRequest) =>
    apiRequest<AuthResponse>("/api/auth/login", {
      method: "POST",
      body: payload,
    }),

  refresh: (refreshToken: string) =>
    apiRequest<AuthTokens>("/api/auth/refresh-token", {
      method: "POST",
      body: { refreshToken },
    }),

  logout: (refreshToken: string) =>
    apiRequest<void>("/api/auth/logout", {
      method: "POST",
      body: { refreshToken },
    }),

  me: () => apiRequest<User>("/api/users/me"),

  completeSocialLogin: (payload: { code?: string; state?: string; token?: string }) =>
    apiRequest<AuthResponse>("/api/auth/oauth2/callback", {
      method: "POST",
      body: payload,
    }),
};
