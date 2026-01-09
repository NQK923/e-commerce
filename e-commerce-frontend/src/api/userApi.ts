import { apiRequest } from "../lib/api-client";
import { User } from "../types/auth";

export const userApi = {
  getById: (id: string) => apiRequest<User>(`/api/users/${id}`),
  updateProfile: (data: { displayName?: string; avatarUrl?: string; shopDescription?: string; shopBannerUrl?: string }) =>
    apiRequest<User>("/api/users/me", { method: "PUT", body: data }),
};
