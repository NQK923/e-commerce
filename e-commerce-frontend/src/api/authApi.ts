import { apiRequest } from "../lib/api-client";
import {
  AuthResponse,
  AuthTokens,
  LoginRequest,
  RegisterRequest,
  ResetPasswordRequest,
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

  requestOtp: (email: string) =>
    apiRequest<{ id: string; email: string; expiresAt: string }>("/api/auth/otp/request", {
      method: "POST",
      body: { email },
    }),

  forgotPassword: (email: string) =>
    apiRequest<{ id?: string; email?: string; expiresAt?: string }>("/api/auth/password/forgot", {
      method: "POST",
      body: { email },
    }),

  resetPassword: (payload: ResetPasswordRequest) =>
    apiRequest<void>("/api/auth/password/reset", {
      method: "POST",
      body: payload,
    }),

  me: () => apiRequest<User>("/api/users/me"),

  completeSocialLogin: (payload: { code?: string; state?: string; token?: string }) =>
    apiRequest<AuthResponse>("/api/auth/oauth2/callback", {
      method: "POST",
      body: payload,
    }),
};
