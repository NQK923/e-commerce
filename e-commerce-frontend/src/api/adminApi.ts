import { apiRequest } from "../lib/api-client";
import { User } from "../types/auth";

export const adminApi = {
  users: () => apiRequest<User[]>("/api/users/all"),
};
