import { apiRequest } from "../lib/api-client";
import { User } from "../types/auth";

export interface OutboxEvent {
  id: string;
  aggregateId: string;
  type: string;
  payload: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  attemptCount: number;
  lastError: string;
  nextRetryAt: string;
  deadLetterAt: string;
}

export interface PaginatedOutboxEvents {
  content: OutboxEvent[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export const adminApi = {
  users: () => apiRequest<User[]>("/api/users/all"),
  outbox: {
    getFailed: (page = 0, size = 10) => 
      apiRequest<PaginatedOutboxEvents>(`/api/admin/outbox/failed?page=${page}&size=${size}`),
    retry: (id: string) => 
      apiRequest<void>(`/api/admin/outbox/${id}/retry`, { method: "POST" }),
  }
};
