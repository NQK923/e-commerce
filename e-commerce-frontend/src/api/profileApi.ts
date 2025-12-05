import { apiRequest } from "../lib/api-client";
import { User, UserAddress } from "../types/auth";
import { Address } from "../types/order";

export const profileApi = {
  me: () => apiRequest<User>("/api/users/me"),
  update: (payload: Partial<Pick<User, "displayName" | "avatarUrl">>) =>
    apiRequest<User>("/api/users/me", { method: "PATCH", body: payload }),
  addAddress: (data: { label: string; isDefault: boolean } & Address) =>
    apiRequest<UserAddress>("/api/users/me/addresses", { method: "POST", body: data }),
  deleteAddress: (addressId: string) =>
    apiRequest<void>(`/api/users/me/addresses/${addressId}`, { method: "DELETE" }),
};

