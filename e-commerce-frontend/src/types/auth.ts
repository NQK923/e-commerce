import { Address } from "./order";

export type AuthProvider = "LOCAL" | "GOOGLE" | "FACEBOOK";

export type UserAddress = {
  id: string;
  label: string;
  isDefault: boolean;
  address: Address;
};

export type User = {
  id: string;
  email: string;
  displayName: string;
  roles: string[];
  provider: AuthProvider;
  avatarUrl?: string;
  shopDescription?: string;
  shopBannerUrl?: string;
  addresses?: UserAddress[];
};

export type AuthTokens = {
  accessToken: string;
  refreshToken: string;
};

export type LoginRequest = {
  email: string;
  password: string;
  otpCode?: string;
  challengeId?: string;
};

export type RegisterRequest = {
  email: string;
  password: string;
  displayName: string;
  otpCode: string;
  challengeId: string;
};

export type ResetPasswordRequest = {
  email: string;
  newPassword: string;
  otpCode: string;
  challengeId: string;
};

export type AuthResponse = {
  user: User;
  accessToken: string;
  refreshToken: string;
  mfaRequired?: boolean;
  challengeId?: string;
  challengeExpiresAt?: string;
  maskedEmail?: string;
};
