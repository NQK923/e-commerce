import { apiRequest } from "../lib/api-client";
import { User } from "../types/auth";

export const userApi = {
  getById: (id: string) => apiRequest<User>(`/api/users/${id}`),
};
