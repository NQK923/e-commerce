export type SellerApplicationStatus = "PENDING" | "APPROVED" | "REJECTED";

export type SellerApplication = {
  id: string;
  userId: string;
  storeName: string;
  contactEmail: string;
  phone: string;
  category?: string;
  description?: string;
  avatarUrl?: string;
  coverUrl?: string;
  status: SellerApplicationStatus;
  acceptedTerms: boolean;
  createdAt?: string;
  updatedAt?: string;
};

export type SubmitSellerApplicationRequest = {
  userId?: string;
  storeName: string;
  email: string;
  phone: string;
  category?: string;
  description?: string;
  acceptedTerms: boolean;
  avatarUrl?: string;
  coverUrl?: string;
};
