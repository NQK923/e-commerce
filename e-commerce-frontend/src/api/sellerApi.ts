import { apiRequest } from "../lib/api-client";
import {
  SellerApplication,
  SellerApplicationStatus,
  SubmitSellerApplicationRequest,
} from "../types/seller";

export const sellerApi = {
  submitApplication: (payload: SubmitSellerApplicationRequest) =>
    apiRequest<SellerApplication>("/api/seller/applications", {
      method: "POST",
      body: {
        userId: payload.userId,
        storeName: payload.storeName,
        email: payload.email,
        phone: payload.phone,
        category: payload.category,
        description: payload.description,
        acceptedTerms: payload.acceptedTerms,
        avatarUrl: payload.avatarUrl,
        coverUrl: payload.coverUrl,
      },
    }),

  listApplications: (status?: SellerApplicationStatus) => {
    const query = status ? `?status=${status}` : "";
    return apiRequest<SellerApplication[]>(`/api/seller/applications${query}`);
  },

  myApplication: () => apiRequest<SellerApplication>("/api/seller/applications/me"),

  approve: (id: string) =>
    apiRequest<SellerApplication>(`/api/seller/applications/${id}/approve`, {
      method: "POST",
    }),

  reject: (id: string) =>
    apiRequest<SellerApplication>(`/api/seller/applications/${id}/reject`, {
      method: "POST",
    }),
};
