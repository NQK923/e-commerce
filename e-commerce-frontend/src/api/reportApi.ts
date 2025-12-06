import { apiRequest } from "../lib/api-client";

export type ReportReason = "FAKE" | "INAPPROPRIATE" | "SCAM" | "OTHER";

export type ReportStatus = "PENDING" | "RESOLVED" | "REJECTED";

export type CreateReportRequest = {
  productId: string;
  reason: ReportReason;
  description?: string;
};

export type ProductReport = {
  id: string;
  productId: string;
  userId: string;
  reason: ReportReason;
  description: string;
  status: ReportStatus;
  createdAt: string;
};

export const reportApi = {
  create: (payload: CreateReportRequest) =>
    apiRequest<void>("/api/reports", { method: "POST", body: payload }),
  
  list: () => 
    apiRequest<ProductReport[]>("/api/reports"),
  
  resolve: (id: string) =>
    apiRequest<void>(`/api/reports/${id}/resolve`, { method: "POST" }),
    
  reject: (id: string) =>
    apiRequest<void>(`/api/reports/${id}/reject`, { method: "POST" }),
};