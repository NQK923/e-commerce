import { apiRequest } from "../lib/api-client";
import { User } from "../types/auth";

export const profileApi = {
  me: () => apiRequest<User>("/api/users/me"),
  update: (payload: Partial<Pick<User, "displayName" | "avatarUrl">>) =>
    apiRequest<User>("/api/users/me", { method: "PATCH", body: payload }),
};
